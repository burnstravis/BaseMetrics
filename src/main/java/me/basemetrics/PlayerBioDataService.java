package me.basemetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class PlayerBioDataService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String UPSERT_SQL = """
        INSERT INTO player_bios (
            player_id, primaryNumber, birthDate, currentAge,
            birthCity, birthCountry, height, weight, active, fullFMLName
        ) VALUES (?,?,?,?,?,?,?,?,?,?)
        ON DUPLICATE KEY UPDATE
            player_id=VALUES(player_id), primaryNumber=VALUES(primaryNumber), birthDate=VALUES(birthDate),
            currentAge=VALUES(currentAge), birthCity=VALUES(birthCity), birthCountry=VALUES(birthCountry),
            height=VALUES(height), weight=VALUES(weight), active=VALUES(active),
            fullFMLName=VALUES(fullFMLName)""";


    public PlayerBio fetchPlayerBioData(int playerId) throws Exception {

        String url = "https://statsapi.mlb.com/api/v1/people/" + playerId;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode peopleNode = MAPPER.readTree(response.body()).path("people");
        if (peopleNode.isArray() && !peopleNode.isEmpty()) {
            return MAPPER.treeToValue(peopleNode.get(0), PlayerBio.class);
        }
        return null;
    }

    public void savePlayerBioBatch(List<PlayerBio> bios, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {
            for (PlayerBio bio : bios) {
                ps.setInt(1, bio.getPlayer_id());
                ps.setString(2, bio.getPrimaryNumber());
                ps.setDate(3, bio.getBirthDate());
                ps.setInt(4, bio.getCurrentAge());
                ps.setString(5, bio.getBirthCity());
                ps.setString(6, bio.getBirthCountry());
                ps.setString(7, bio.getHeight());
                ps.setInt(8, bio.getWeight());
                ps.setBoolean(9, bio.isActive());
                ps.setString(10, bio.getFullFMLName());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }

    public List<Integer> getAllPlayerIds(){

        List<Integer> playerIds = new ArrayList<>();
        String query = "SELECT player_id FROM players";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                playerIds.add(rs.getInt("player_id"));
            }

        } catch (Exception e) {
            System.err.println("Error fetching player IDs: " + e.getMessage());
            e.printStackTrace();
        }

        return playerIds;
    }

}
