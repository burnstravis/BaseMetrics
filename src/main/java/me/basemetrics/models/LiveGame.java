package me.basemetrics.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;

@Entity
@Table(name = "live_games")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveGame {

    @Id
    private int game_id;

    @Column(name = "home_team")
    private String home_team_name;

    @Column(name = "away_team")
    private String away_team_name;

    private int home_score;
    private int home_hits;
    private int away_score;
    private int away_hits;
    private int inning;
    private String inning_half;
    private int outs;
    private String batter;
    private String pitcher;
    private boolean on_first;
    private boolean on_second;
    private boolean on_third;
    private int home_errors;
    private int away_errors;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private Timestamp last_updated;

    protected LiveGame() {}

    @JsonCreator
    public LiveGame(
            @JsonProperty("gamePk") int game_id,
            @JsonProperty("liveData") JsonNode liveData
    )
    {
        this.game_id = game_id;

        if (liveData != null) {
            JsonNode linescore = liveData.path("linescore");
            this.inning = linescore.path("currentInning").asInt();
            this.inning_half = linescore.path("inningHalf").asText();
            this.outs = linescore.path("outs").asInt();

            JsonNode offense = linescore.path("offense");
            this.on_first = offense.has("first");
            this.on_second = offense.has("second");
            this.on_third = offense.has("third");
            this.batter = offense.path("batter").path("fullName").asText("N/A");
            this.pitcher = linescore.path("defense").path("pitcher").path("fullName").asText("N/A");

            JsonNode boxTeams = liveData.path("boxscore").path("teams");
            JsonNode lineTeams = linescore.path("teams");

            this.away_team_name = boxTeams.path("away").path("team").path("name").asText("Away");
            this.away_score = lineTeams.path("away").path("runs").asInt();
            this.away_errors = lineTeams.path("away").path("errors").asInt();
            this.away_hits = lineTeams.path("away").path("hits").asInt();


            this.home_team_name = boxTeams.path("home").path("team").path("name").asText("Home");
            this.home_score = lineTeams.path("home").path("runs").asInt();
            this.home_errors = lineTeams.path("home").path("errors").asInt();
            this.home_hits = lineTeams.path("home").path("hits").asInt();
        }
    }

    // Getters
    public int getGame_id() { return game_id; }
    public String getHome_team_name() { return home_team_name; }
    public String getAway_team_name() { return away_team_name; }
    public int getHome_score() { return home_score; }
    public int getAway_score() { return away_score; }
    public int getHome_hits() {
        return home_hits;
    }
    public int getAway_hits() {
        return away_hits;
    }
    public int getInning() { return inning; }
    public String getInning_half() { return inning_half; }
    public int getOuts() { return outs; }
    public String getBatter() { return batter; }
    public String getPitcher() { return pitcher; }
    public boolean isOn_first() { return on_first; }
    public boolean isOn_second() { return on_second; }
    public boolean isOn_third() { return on_third; }
    public int getHome_errors() { return home_errors; }
    public int getAway_errors() { return away_errors; }
}