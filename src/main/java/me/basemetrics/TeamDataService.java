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
import java.util.ArrayList;
import java.util.List;

public class TeamDataService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String UPSERT_SQL = """
        INSERT INTO teams (
            id, name, abbreviation, location, image_url
        ) VALUES (?,?,?,?,?)
        ON DUPLICATE KEY UPDATE 
            id=VALUES(id), name=VALUES(name), abbreviation=VALUES(abbreviation), 
            location=VALUES(location), image_url=VALUES(image_url)
        """;

    //returns JSON URL for a teams 40 man roster
    private String getRosterUrl(int teamId){
        return "https://statsapi.mlb.com/api/v1/teams/" + teamId + "/roster/40Man";
    }

    //returns JSON URL for a teams BIO/INFO
    private String getTeamUrl(int teamId) {
        return "https://statsapi.mlb.com/api/v1/teams/" + teamId;
    }

    //returns list of (player ids) from a teams roster
    public List<Integer> fetchRosterIds(int teamId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(getRosterUrl(teamId))).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        JsonNode root = MAPPER.readTree(response.body());

        List<Integer> ids = new ArrayList<>();

        for (JsonNode entry : root.path("roster")) {
            int id = entry.path("person").path("id").asInt();
            if (id > 0) ids.add(id);
        }
        return ids;
    }

    public Team fetchTeamData(int teamId) throws Exception {

        String url = getTeamUrl(teamId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode teamNode = MAPPER.readTree(response.body()).path("teams");
        if (teamNode.isArray() && !teamNode.isEmpty()) {
            return MAPPER.treeToValue(teamNode.get(0), Team.class);
        }
        return null;
    }

    public void saveTeamBatch(List<Team> teams, Connection conn) throws SQLException {
        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = conn.prepareStatement(UPSERT_SQL)) {
            for (Team t : teams) {

                pstmt.setInt(1, t.getId());
                pstmt.setString(2, t.getName());
                pstmt.setString(3, t.getAbbreviation());
                pstmt.setString(4, t.getLocation());
                pstmt.setString(5, t.getImage_url());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            conn.rollback();      // Undo if anything fails
            throw e;
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

}