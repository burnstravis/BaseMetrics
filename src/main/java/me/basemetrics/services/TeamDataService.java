package me.basemetrics.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.basemetrics.models.Team;
import me.basemetrics.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;

@Service
public class TeamDataService {

    @Autowired
    private Semaphore mlbApiSemaphore;

    @Autowired
    private TeamRepository teamRepository;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //MLB API URLS
    private String getRosterUrl(int teamId){
        return "https://statsapi.mlb.com/api/v1/teams/" + teamId + "/roster/40Man";
    }
    private String getTeamUrl(int teamId) {
        return "https://statsapi.mlb.com/api/v1/teams/" + teamId;
    }

    //returns list of (player ids) from a teams roster
    public List<Integer> fetchRosterIds(int teamId) throws Exception {

        try {
            mlbApiSemaphore.acquire();

            HttpRequest request = HttpRequest.newBuilder(URI.create(getRosterUrl(teamId))).build();
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            JsonNode root = MAPPER.readTree(response.body());

            List<Integer> ids = new ArrayList<>();

            for (JsonNode entry : root.path("roster")) {
                int id = entry.path("person").path("id").asInt();
                if (id > 0) ids.add(id);
            }
            return ids;
        } catch (Exception e) {
            System.err.println("Error fetching roster for team " + teamId + ": " + e.getMessage());
            return null;
        } finally {
            mlbApiSemaphore.release();
        }

    }

    public Team fetchTeamData(int teamId) throws Exception {

        try {
            mlbApiSemaphore.acquire();

            String url = getTeamUrl(teamId);

            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            JsonNode teamNode = MAPPER.readTree(response.body()).path("teams");
            if (teamNode.isArray() && !teamNode.isEmpty()) {
                return MAPPER.treeToValue(teamNode.get(0), Team.class);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error for team " + teamId + ": " + e.getMessage());
            return null;
        } finally {
            mlbApiSemaphore.release();
        }

    }


    public static List<Integer> getTeamIds() {
        return new ArrayList<>(List.of(
                108, 109, 110, 111, 112, 113, 114,
                115, 116, 117, 118, 119, 120, 121,
                133, 134, 135, 136, 137, 138, 139,
                140, 141, 142, 143, 144, 145, 146,
                147, 158
        ));
    }

    //@Scheduled(cron = "0 0 5 * * *")
    public void updateAllTeams(){

        List<Integer> teamIds = getTeamIds();

        List<Team> teams;

        try (var scope = StructuredTaskScope.open()) {

            List<StructuredTaskScope.Subtask<Team>> tasks = teamIds.stream()
                    .map(id -> scope.fork(() -> fetchTeamData(id)))
                    .toList();

            scope.join();

            teams = tasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            teamRepository.saveAll(teams);
            System.out.println("Successfully updated " + teams.size() + " teams using JPA.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}