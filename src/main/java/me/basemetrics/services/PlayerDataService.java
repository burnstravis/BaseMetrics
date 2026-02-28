package me.basemetrics.services;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.basemetrics.models.Player;
import me.basemetrics.repositories.PlayerRepository;
import me.basemetrics.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerDataService {

    @Autowired
    private Semaphore mlbApiSemaphore;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamDataService teamService;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();


    //@Scheduled(initialDelay = 15000, fixedRate = 43200000)
    @Transactional
    public void updateAllPlayers() {
        Map<Integer, Integer> playerToTeamMap = new java.util.HashMap<>();

        try (var scope = StructuredTaskScope.open()) {
            List<Integer> teamIds = TeamDataService.getTeamIds();

            record RosterResult(int teamId, List<Integer> playerIds) {}

            List<StructuredTaskScope.Subtask<RosterResult>> rosterTasks = teamIds.stream()
                    .map(teamId -> scope.fork(() ->
                            new RosterResult(teamId, teamService.fetchRosterIds(teamId))
                    ))
                    .toList();

            scope.join();

            for (var task : rosterTasks) {
                RosterResult res = task.get();
                for (Integer pId : res.playerIds()) {
                    playerToTeamMap.put(pId, res.teamId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching rosters: " + e.getMessage());
            return;
        }

        try (var scope = StructuredTaskScope.open()) {
            List<StructuredTaskScope.Subtask<Player>> playerTasks = playerToTeamMap.entrySet().stream()
                    .map(entry -> scope.fork(() -> fetchPlayerData(entry.getKey(), entry.getValue())))
                    .toList();

            scope.join();

            List<Player> players = playerTasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (!players.isEmpty()) {
                playerRepository.saveAll(players);

                List<Integer> activeIds = playerToTeamMap.keySet().stream().toList();
                syncDatabaseRosters(activeIds);

                System.out.println("Successfully saved " + players.size() + " players.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching player data: " + e.getMessage());
        }
    }

    public Player fetchPlayerData(int playerId, int knownTeamId) {
        try {
            mlbApiSemaphore.acquire();

            String url = "https://statsapi.mlb.com/api/v1/people/" + playerId +
                    "?hydrate=stats(group=[hitting,pitching],type=[season],season=2025)";

            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            JsonNode root = MAPPER.readTree(response.body());
            JsonNode personNode = root.path("people").get(0);

            Player player = MAPPER.treeToValue(personNode, Player.class);

            if (player != null) {
                player.setTeam_id(knownTeamId);
            }

            return player;
        } catch (Exception e) {
            return null;
        } finally {
            mlbApiSemaphore.release();
        }
    }

    private void syncDatabaseRosters(List<Integer> activeIds) {
        List<Player> allDbPlayers = playerRepository.findAll();
        List<Player> toDelete = allDbPlayers.stream()
                .filter(p -> !activeIds.contains(p.getPlayer_id()))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            playerRepository.deleteAll(toDelete);
            System.out.println("Cleaned up " + toDelete.size() + " non-rostered players.");
        }
    }

}