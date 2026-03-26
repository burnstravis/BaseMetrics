package me.basemetrics.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.basemetrics.models.LiveGame;
import me.basemetrics.repositories.LiveGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
public class LiveGameService {

    @Autowired
    private LiveGameRepository liveGameRepository;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final boolean TODAY = true;
    public static final boolean YESTERDAY = false;


    @Scheduled(initialDelay = 60000, fixedRate = 15000)
    @Transactional
    @CacheEvict(value = "liveGames", allEntries = true)
    public void updateAllLiveGames() {
        try {

            LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
            liveGameRepository.deleteOldGames(cutoff);

            List<Integer> liveGameIds = getAllLiveGameIds(TODAY);
            liveGameIds.addAll(getAllLiveGameIds(YESTERDAY));
            System.out.println("Combined Game Ids Between Yesterday and Today: " + liveGameIds);


            if (liveGameIds.isEmpty()) return;

            try (var scope = StructuredTaskScope.open()) {
                List<StructuredTaskScope.Subtask<LiveGame>> tasks = liveGameIds.stream()
                        .map(gameId -> scope.fork(() -> fetchLiveGameData(gameId)))
                        .toList();

                scope.join();

                List<LiveGame> games = tasks.stream()
                        .filter(t -> t.state() == StructuredTaskScope.Subtask.State.SUCCESS)
                        .map(StructuredTaskScope.Subtask::get)
                        .filter(java.util.Objects::nonNull)
                        .toList();

                liveGameRepository.saveAll(games);
                System.out.println("Successfully updated " + games.size() + " live games.");
            }
        } catch (Exception e) {
            System.err.println("Error updating live games: " + e.getMessage());
        }
    }

    public String getAllLiveGamesUrl(boolean day){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter;

        if(day == YESTERDAY){
            now = now.minusDays(1);
        }

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = now.format(formatter);
        return "https://statsapi.mlb.com/api/v1/schedule?sportId=1&date=" + formattedDate;
    }

    //returns list of all gameIds for the day
    public List<Integer> getAllLiveGameIds(boolean day) throws Exception{
        String url = getAllLiveGamesUrl(day);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode root = MAPPER.readTree(response.body());

        List<Integer> gameIds = new ArrayList<>();
        JsonNode datesNode = root.path("dates");

        for (JsonNode dateEntry : datesNode) {
            JsonNode gamesNode = dateEntry.path("games");
            for (JsonNode game : gamesNode) {
                int id = game.path("gamePk").asInt();
                if(day == YESTERDAY && !game.path("status").path("detailedState").asText().equals("Final") && id > 0){
                    gameIds.add(id);
                }
                else if (day == TODAY && id > 0){
                    gameIds.add(id);
                }

            }
        }
        return gameIds;
    }

    public String getLiveGameUrl(int gameId){
        return "https://statsapi.mlb.com/api/v1.1/game/" + gameId + "/feed/live";
    }

    //get and stores JSON
    public LiveGame fetchLiveGameData(int gameId) throws Exception {

        String url = getLiveGameUrl(gameId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode root = MAPPER.readTree(response.body());

        if(root != null && !root.isMissingNode()){
            return MAPPER.treeToValue(root, LiveGame.class);
        }

        return null;
    }

    @Cacheable(value = "liveGames", key = "'all'")
    public List<LiveGame> getLiveGamesFromDb() {
        return liveGameRepository.findAll();
    }

}
