package me.basemetrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveGame {

    private static final ObjectMapper INTERNAL_MAPPER = new ObjectMapper();

    private int game_id;
    private int inning;
    private String inning_half;
    private int outs;
    private String batter;
    private String pitcher;
    private boolean on_first;
    private boolean on_second;
    private boolean on_third;

    private LiveGameTeam home_team;
    private LiveGameTeam away_team;

    @JsonCreator
    public LiveGame(
            @JsonProperty("gamePk") int game_id,
            @JsonProperty("liveData") JsonNode liveData
    ) {
        this.game_id = game_id;

        if (liveData != null) {
            JsonNode linescore = liveData.path("linescore");

            this.inning = linescore.path("currentInning").asInt();
            this.inning_half = linescore.path("inningHalf").asText();
            this.outs = linescore.path("outs").asInt();

            JsonNode offense = linescore.path("offense");
            this.on_first = offense.has("first");   // Returns true if the "first" node exists
            this.on_second = offense.has("second");
            this.on_third = offense.has("third");
            this.batter = offense.path("batter").path("fullName").asText("N/A");

            this.pitcher = linescore.path("defense").path("pitcher").path("fullName").asText("N/A");

            JsonNode boxTeams = liveData.path("boxscore").path("teams");
            JsonNode lineTeams = linescore.path("teams");

            this.away_team = new LiveGameTeam(
                    boxTeams.path("away").path("team").path("name").asText("Away"),
                    lineTeams.path("away").path("runs").asInt(),
                    lineTeams.path("away").path("errors").asInt()
            );

            this.home_team = new LiveGameTeam(
                    boxTeams.path("home").path("team").path("name").asText("Home"),
                    lineTeams.path("home").path("runs").asInt(),
                    lineTeams.path("home").path("errors").asInt()
            );
        }
    }


    public int getGame_id() {
        return game_id;
    }
    public int getInning() {
        return inning;
    }
    public String getInning_half() {
        return inning_half;
    }
    public int getOuts() {
        return outs;
    }
    public String getBatter() {
        return batter;
    }
    public String getPitcher() {
        return pitcher;
    }
    public boolean isOn_first() {
        return on_first;
    }
    public boolean isOn_second() {
        return on_second;
    }
    public boolean isOn_third() {
        return on_third;
    }
    public LiveGameTeam getHome_team() {
        return home_team;
    }
    public LiveGameTeam getAway_team() {
        return away_team;
    }

    public int getHome_score() { return home_team != null ? home_team.getScore() : 0; }
    public int getAway_score() { return away_team != null ? away_team.getScore() : 0; }
    public int getHome_errors() { return home_team != null ? home_team.getErrors() : 0; }
    public int getAway_errors() { return away_team != null ? away_team.getErrors() : 0; }
    public String getHome_team_name() { return home_team != null ? home_team.getName() : "Home"; }
    public String getAway_team_name() { return away_team != null ? away_team.getName() : "Away"; }
}
