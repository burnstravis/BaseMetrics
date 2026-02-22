package me.basemetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LiveGameService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String UPSERT_SQL = """
        INSERT INTO live_games (
            game_id, home_team, away_team, home_score, 
            away_score, inning, inning_half, 
            outs, batter, pitcher, on_first, 
            on_second, on_third, home_errors, 
            away_errors, last_updated
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())
        ON DUPLICATE KEY UPDATE
            home_score=VALUES(home_score), away_score=VALUES(away_score), inning=VALUES(inning), 
            inning_half=VALUES(inning_half), outs=VALUES(outs), batter=VALUES(batter), 
            pitcher=VALUES(pitcher), on_first=VALUES(on_first), on_second=VALUES(on_second), 
            on_third=VALUES(on_third), home_errors=VALUES(home_errors), away_errors=VALUES(away_errors), 
            last_updated=NOW()
        """;

    public String getAllLiveGamesUrl(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String formattedDate = now.format(formatter);
        return "https://statsapi.mlb.com/api/v1/schedule?sportId=1&date=" + formattedDate;
    }

    //returns list of all gameIds for the day
    public List<Integer> getAllLiveGameIds() throws Exception{
        String url = getAllLiveGamesUrl();

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode root = MAPPER.readTree(response.body());

        List<Integer> gameIds = new ArrayList<>();
        JsonNode datesNode = root.path("dates");

        for (JsonNode dateEntry : datesNode) {
            JsonNode gamesNode = dateEntry.path("games");
            for (JsonNode game : gamesNode) {
                int id = game.path("gamePk").asInt();
                if (id > 0) {
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

    //inserts and updated into SQL table
    public void saveLiveGameBatch(List<LiveGame> liveGames, Connection conn) throws SQLException {

        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = conn.prepareStatement(UPSERT_SQL)) {
            for (LiveGame g : liveGames) {

                pstmt.setInt(1, g.getGame_id());
                pstmt.setString(2, g.getHome_team_name());
                pstmt.setString(3, g.getAway_team_name());
                pstmt.setInt(4, g.getHome_score());
                pstmt.setInt(5, g.getAway_score());

                pstmt.setInt(6, g.getInning());
                pstmt.setString(7, g.getInning_half());
                pstmt.setInt(8, g.getOuts());
                pstmt.setString(9, g.getBatter());
                pstmt.setString(10, g.getPitcher());

                pstmt.setBoolean(11, g.isOn_first());
                pstmt.setBoolean(12, g.isOn_second());
                pstmt.setBoolean(13, g.isOn_third());

                pstmt.setInt(14, g.getHome_errors());
                pstmt.setInt(15, g.getAway_errors());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            conn.rollback();      // Undo if anything fails
            throw e;
        }

    }

}
