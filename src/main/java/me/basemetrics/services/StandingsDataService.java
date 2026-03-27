package me.basemetrics.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.basemetrics.models.Standings;
import me.basemetrics.repositories.StandingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Service
public class StandingsDataService {

    @Autowired
    private Semaphore mlbApiSemaphore;

    @Autowired
    private StandingsRepository standingsRepository;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String getStandingsUrl(){

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = now.format(formatter);
        String year = String.valueOf(now.getYear());
        return "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season=" + year + "&standingsTypes=regularSeason&date=" + date;
    }

    public static String getExtraStatsUrl(){
        return "https://statsapi.mlb.com/api/v1/teams";
    }

    public List<Standings> fetchStandings() throws Exception {
        try {
            mlbApiSemaphore.acquire();

            HttpRequest teamRequest = HttpRequest.newBuilder(URI.create(getExtraStatsUrl())).build();
            var teamResponse = CLIENT.send(teamRequest, HttpResponse.BodyHandlers.ofInputStream());
            JsonNode teamRoot = MAPPER.readTree(teamResponse.body());

            Map<Integer, JsonNode> teamLookup = new HashMap<>();
            for (JsonNode t : teamRoot.path("teams")) {
                teamLookup.put(t.path("id").asInt(), t);
            }

            String url = getStandingsUrl();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            JsonNode root = MAPPER.readTree(response.body());
            JsonNode records = root.path("records");

            List<Standings> allStandings = new ArrayList<>();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            for (JsonNode record : records) {
                JsonNode teamRecords = record.path("teamRecords");
                for (JsonNode teamRecord : teamRecords) {
                    int teamId = teamRecord.path("team").path("id").asInt();

                    JsonNode extraData = teamLookup.get(teamId);
                    JsonNode streakNode = teamRecord.path("streak");
                    JsonNode recordsNode = teamRecord.path("records");

                    Standings standing = new Standings(
                            teamRecord.path("team"),
                            extraData != null ? extraData.path("league") : null,
                            extraData != null ? extraData.path("division") : null,
                            teamRecord.path("team").path("name").asText(),
                            teamRecord.path("wins").asInt(),
                            teamRecord.path("losses").asInt(),
                            now,
                            (float) teamRecord.path("gamesBack").asDouble(),
                            (float) teamRecord.path("wildCardGamesBack").asDouble(),
                            (float) teamRecord.path("leagueGamesBack").asDouble(),
                            streakNode,
                            recordsNode
                    );

                    allStandings.add(standing);
                }
            }

            if (!allStandings.isEmpty()) {
                standingsRepository.saveAll(allStandings);
            }

            return allStandings;
        } catch (Exception e) {
            System.err.println("Fetch error: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            mlbApiSemaphore.release();
        }
    }

    public void updateStandings() {
        try {
            List<Standings> updatedStandings = fetchStandings();

            if (updatedStandings != null && !updatedStandings.isEmpty()) {
                System.out.println("Successfully updated " + updatedStandings.size() + " team standings.");
            }
        } catch (Exception e) {
            System.err.println("Critical error during standings update: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
