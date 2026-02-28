package me.basemetrics.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.basemetrics.models.Player;
import me.basemetrics.models.PlayerBio;
import me.basemetrics.repositories.PlayerBioRepository;
import me.basemetrics.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;

@Service
public class PlayerBioDataService {

    @Autowired
    private Semaphore mlbApiSemaphore;

    @Autowired
    private PlayerBioRepository playerBioRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //@Scheduled(initialDelay = 60000, fixedRate = 604800000)
    @Transactional
    public void updateAllPlayerBios() {

        System.out.println("Starting mass Player Bio update...");

        List<Integer> playerIds = playerRepository.findAll().stream()
                .map(Player::getPlayer_id)
                .toList();

        if (playerIds.isEmpty()) {
            System.out.println("No players found in database to fetch bios for.");
            return;
        }

        try (var scope = StructuredTaskScope.open()) {
            List<StructuredTaskScope.Subtask<PlayerBio>> tasks = playerIds.stream()
                    .map(id -> scope.fork(() -> fetchPlayerBioData(id)))
                    .toList();

            scope.join();

            List<PlayerBio> bios = tasks.stream()
                    .filter(t -> t.state() == StructuredTaskScope.Subtask.State.SUCCESS)
                    .map(StructuredTaskScope.Subtask::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            playerBioRepository.saveAll(bios);
            System.out.println("Successfully updated " + bios.size() + " player bios.");

        } catch (Exception e) {
            System.err.println("Error during bio sync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PlayerBio fetchPlayerBioData(int playerId) throws Exception {

        try {
            mlbApiSemaphore.acquire();

            String url = "https://statsapi.mlb.com/api/v1/people/" + playerId;

            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            JsonNode peopleNode = MAPPER.readTree(response.body()).path("people");
            if (peopleNode.isArray() && !peopleNode.isEmpty()) {
                return MAPPER.treeToValue(peopleNode.get(0), PlayerBio.class);
            }
            return null;

        } catch (Exception e) {
            System.err.println("Error for player " + playerId + ": " + e.getMessage());
            return null;
        } finally {
            mlbApiSemaphore.release();
        }

    }


}
