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
import java.util.List;

public class PlayerDataService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String UPSERT_SQL = """
        INSERT INTO players (
            player_id, name, team_id, position, batting_avg, 
            home_runs, rbi, image_url, plate_appearances, base_on_balls, 
            stolen_bases, caught_stealing, hit_by_pitch, strike_outs, doubles, 
            triples, slg, obp, ops, at_bats, games_played, 
            runs, gidp, p_wins, p_losses, p_era, p_ip, 
            p_hits, p_earned_runs, p_whip, p_batters_faced, p_games, 
            p_home_runs, p_walks, p_strikeouts, p_runs, p_gidp, p_sb, p_hit_by_pitch
        ) VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)
        ON DUPLICATE KEY UPDATE 
            name=VALUES(name), team_id=VALUES(team_id), position=VALUES(position), 
            batting_avg=VALUES(batting_avg), home_runs=VALUES(home_runs), rbi=VALUES(rbi), 
            image_url=VALUES(image_url), plate_appearances=VALUES(plate_appearances), 
            base_on_balls=VALUES(base_on_balls), stolen_bases=VALUES(stolen_bases), 
            caught_stealing=VALUES(caught_stealing), hit_by_pitch=VALUES(hit_by_pitch), 
            strike_outs=VALUES(strike_outs), doubles=VALUES(doubles), triples=VALUES(triples), 
            slg=VALUES(slg), obp=VALUES(obp), ops=VALUES(ops), at_bats=VALUES(at_bats), 
            games_played=VALUES(games_played), runs=VALUES(runs), gidp=VALUES(gidp), 
            p_wins=VALUES(p_wins), p_losses=VALUES(p_losses), p_era=VALUES(p_era), 
            p_ip=VALUES(p_ip), p_hits=VALUES(p_hits), p_earned_runs=VALUES(p_earned_runs), 
            p_whip=VALUES(p_whip), p_batters_faced=VALUES(p_batters_faced), 
            p_home_runs=VALUES(p_home_runs), p_walks=VALUES(p_walks), 
            p_strikeouts=VALUES(p_strikeouts), p_games=VALUES(p_games), 
            p_runs=VALUES(p_runs), p_gidp=VALUES(p_gidp), p_sb=VALUES(p_sb), 
            p_hit_by_pitch=VALUES(p_hit_by_pitch)
        """;

    public Player fetchPlayerData(int playerId) throws Exception {
        String url = "https://statsapi.mlb.com/api/v1/people/" + playerId +
                "?hydrate=stats(group=[hitting,pitching],type=[season],season=2025)";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode personNode = root.path("people").get(0);

        return MAPPER.treeToValue(personNode, Player.class);
    }

    public void savePlayersBatch(List<Player> players, Connection conn, int teamId) throws SQLException {
        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = conn.prepareStatement(UPSERT_SQL)) {
            for (Player p : players) {

                pstmt.setInt(1, p.getPlayer_id());
                pstmt.setString(2, p.getName());
                pstmt.setInt(3, teamId);
                pstmt.setString(4, p.getPosition());

                BattingStats b = p.getBattingStats();
                if (b != null) {
                    pstmt.setFloat(5, b.getBatting_avg());
                    pstmt.setInt(6, b.getHome_runs());
                    pstmt.setInt(7, b.getRbi());
                    pstmt.setInt(9, b.getPlate_appearances());
                    pstmt.setInt(10, b.getBase_on_balls());
                    pstmt.setInt(11, b.getStolen_bases());
                    pstmt.setInt(12, b.getCaught_stealing());
                    pstmt.setInt(13, b.getHit_by_pitch());
                    pstmt.setInt(14, b.getStrike_outs());
                    pstmt.setInt(15, b.getDoubles());
                    pstmt.setInt(16, b.getTriples());
                    pstmt.setString(17, b.getSlg());
                    pstmt.setString(18, b.getObp());
                    pstmt.setString(19, b.getOps());
                    pstmt.setInt(20, b.getAt_bats());
                    pstmt.setInt(21, b.getGames_played());
                    pstmt.setInt(22, b.getRuns());
                    pstmt.setInt(23, b.getGidp());
                } else {
                    pstmt.setFloat(5, 0.0f); pstmt.setInt(6, 0); pstmt.setInt(7, 0);
                    pstmt.setInt(9, 0); pstmt.setInt(10, 0); pstmt.setInt(11, 0);
                    pstmt.setInt(12, 0); pstmt.setInt(13, 0); pstmt.setInt(14, 0);
                    pstmt.setInt(15, 0); pstmt.setInt(16, 0);
                    pstmt.setString(17, ".000"); pstmt.setString(18, ".000"); pstmt.setString(19, ".000");
                    pstmt.setInt(20, 0); pstmt.setInt(21, 0); pstmt.setInt(22, 0); pstmt.setInt(23, 0);
                }

                pstmt.setString(8, p.getImage_url());

                PitchingStats ps = p.getPitchingStats();
                if (ps != null) {
                    pstmt.setInt(24, ps.getP_wins());
                    pstmt.setInt(25, ps.getP_losses());
                    pstmt.setFloat(26, ps.getP_era());
                    pstmt.setString(27, ps.getP_ip());
                    pstmt.setInt(28, ps.getP_hits());
                    pstmt.setInt(29, ps.getP_earned_runs());
                    pstmt.setFloat(30, ps.getP_whip());
                    pstmt.setInt(31, ps.getP_batters_faced());
                    pstmt.setInt(32, ps.getP_games());
                    pstmt.setInt(33, ps.getP_homer_runs());
                    pstmt.setInt(34, ps.getP_walks());
                    pstmt.setInt(35, ps.getP_strikeouts());
                    pstmt.setInt(36, ps.getP_runs());
                    pstmt.setInt(37, ps.getP_gidp());
                    pstmt.setInt(38, ps.getP_sb());
                    pstmt.setInt(39, ps.getP_hit_by_pitch());
                } else {
                    pstmt.setInt(24, 0); pstmt.setInt(25, 0); pstmt.setFloat(26, 0.0f);
                    pstmt.setString(27, "0.0");
                    for (int i = 28; i <= 39; i++) pstmt.setInt(i, 0);
                }

                pstmt.addBatch(); //Adds player to the buffer
            }

            pstmt.executeBatch(); //Executes all 40 inserts in one trip
            conn.commit();        // Finalizes the transaction

        } catch (SQLException e) {
            conn.rollback();      // Undo if anything fails
            throw e;
        }
    }

}