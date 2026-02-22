package me.basemetrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    private static final ObjectMapper INTERNAL_MAPPER = new ObjectMapper();

    private int player_id;
    private String name;
    private int team_id;
    private String position;
    private String image_url;
    private BattingStats battingStats;
    private PitchingStats pitchingStats;

    @JsonCreator
    public Player(
            @JsonProperty("id") int player_id,
            @JsonProperty("fullName") String name,
            @JsonProperty("currentTeam") JsonNode teamNode, // array of team details
            @JsonProperty("primaryPosition") JsonNode posNode, // array of position details
            @JsonProperty("stats") JsonNode statsNode // array of stats
    ) {
        this.player_id = player_id;
        this.name = name;
        this.team_id = teamNode != null ? teamNode.path("id").asInt() : 0;
        this.position = posNode != null ? posNode.path("abbreviation").asText() : "N/A";
        this.image_url = "https://img.mlbstatic.com/mlb-photos/image/upload/v1/people/" + player_id + "/headshot/67/current.png";


        if (statsNode != null && statsNode.isArray()) {
            for (JsonNode statEntry : statsNode) {
                String group = statEntry.path("group").path("displayName").asText();
                JsonNode firstSplit = statEntry.path("splits").path(0).path("stat");

                if ("hitting".equals(group)) {
                    this.battingStats = INTERNAL_MAPPER.convertValue(firstSplit, BattingStats.class);
                } else if ("pitching".equals(group)) {
                    this.pitchingStats = INTERNAL_MAPPER.convertValue(firstSplit, PitchingStats.class);
                }
            }
        }
    }



    public int getPlayer_id() { return player_id; }
    public String getName() { return name; }
    public int getTeam_id() { return team_id; }
    public String getPosition() { return position; }
    public String getImage_url() { return image_url; }
    public BattingStats getBattingStats() { return battingStats; }
    public PitchingStats getPitchingStats() { return pitchingStats; }
}