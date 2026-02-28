package me.basemetrics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class BattingStats {

    private float batting_avg;
    private int home_runs;
    private int rbi;
    private int plate_appearances;
    private int base_on_balls;
    private int stolen_bases;
    private int caught_stealing;
    private int hit_by_pitch;
    private int strike_outs;
    private int doubles;
    private int triples;
    private String slg;
    private String obp;
    private String ops;
    private int at_bats;
    private int games_played;
    private int runs;
    private int gidp;

    protected BattingStats() {}

    @JsonCreator
    public BattingStats(
            @JsonProperty("avg") float batting_avg,
            @JsonProperty("homeRuns") int home_runs,
            @JsonProperty("rbi") int rbi,
            @JsonProperty("plateAppearances") int plate_appearances,
            @JsonProperty("baseOnBalls") int base_on_balls,
            @JsonProperty("stolenBases") int stolen_bases,
            @JsonProperty("caughtStealing") int caught_stealing,
            @JsonProperty("hitByPitch") int hit_by_pitch,
            @JsonProperty("strikeOuts") int strike_outs,
            @JsonProperty("doubles") int doubles,
            @JsonProperty("triples") int triples,
            @JsonProperty("slg") String slg,
            @JsonProperty("obp") String obp,
            @JsonProperty("ops") String ops,
            @JsonProperty("atBats") int at_bats,
            @JsonProperty("gamesPlayed") int games_played,
            @JsonProperty("runs") int runs,
            @JsonProperty("groundIntoDoublePlay") int gidp)
    {
        this.batting_avg = batting_avg;
        this.home_runs = home_runs;
        this.rbi = rbi;
        this.plate_appearances = plate_appearances;
        this.base_on_balls = base_on_balls;
        this.stolen_bases = stolen_bases;
        this.caught_stealing = caught_stealing;
        this.hit_by_pitch = hit_by_pitch;
        this.strike_outs = strike_outs;
        this.doubles = doubles;
        this.triples = triples;
        this.slg = slg;
        this.obp = obp;
        this.ops = ops;
        this.at_bats = at_bats;
        this.games_played = games_played;
        this.runs = runs;
        this.gidp = gidp;
    }


    public float getBatting_avg() {
        return batting_avg;
    }
    public int getHome_runs() {
        return home_runs;
    }
    public int getRbi() {
        return rbi;
    }
    public int getPlate_appearances() {
        return plate_appearances;
    }
    public int getStolen_bases() {
        return stolen_bases;
    }
    public int getBase_on_balls() {
        return base_on_balls;
    }
    public int getCaught_stealing() {
        return caught_stealing;
    }
    public int getStrike_outs() {
        return strike_outs;
    }
    public int getHit_by_pitch() {
        return hit_by_pitch;
    }
    public int getDoubles() {
        return doubles;
    }
    public int getTriples() {
        return triples;
    }
    public String getSlg() {
        return slg;
    }
    public String getObp() {
        return obp;
    }
    public String getOps() {
        return ops;
    }
    public int getAt_bats() {
        return at_bats;
    }
    public int getGames_played() {
        return games_played;
    }
    public int getRuns() {
        return runs;
    }
    public int getGidp() {
        return gidp;
    }

}
