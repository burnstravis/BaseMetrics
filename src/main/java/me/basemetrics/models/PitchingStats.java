package me.basemetrics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class PitchingStats {

    private int p_wins;
    private int p_losses;
    private float p_era;
    private String p_ip;
    private int p_hits;
    private int p_earned_runs;
    private float p_whip;
    private int p_batters_faced;
    private int p_home_runs;
    private int p_walks;
    private int p_strikeouts;
    private int p_games;
    private int p_runs;
    private int p_gidp;
    private int p_sb;
    private int p_hit_by_pitch;

    protected PitchingStats() {}

    @JsonCreator
    public PitchingStats(
            @JsonProperty("wins") int p_wins,
            @JsonProperty("losses") int p_losses,
            @JsonProperty("era") float p_era,
            @JsonProperty("inningsPitched") String p_ip,
            @JsonProperty("hits") int p_hits,
            @JsonProperty("earnedRuns") int p_earned_runs,
            @JsonProperty("whip") float p_whip,
            @JsonProperty("battersFaced") int p_batters_faced,
            @JsonProperty("homeRuns") int p_homer_runs,
            @JsonProperty("baseOnBalls") int p_walks,
            @JsonProperty("strikeOuts") int p_strikeouts,
            @JsonProperty("gamesPlayed") int p_games,
            @JsonProperty("runs") int p_runs,
            @JsonProperty("groundIntoDoublePlay") int p_gidp,
            @JsonProperty("stolenBases") int p_sb,
            @JsonProperty("hitByPitch") int p_hit_by_pitch)
    {
        this.p_wins = p_wins;
        this.p_losses = p_losses;
        this.p_era = p_era;
        this.p_ip = p_ip;
        this.p_hits = p_hits;
        this.p_earned_runs = p_earned_runs;
        this.p_whip = p_whip;
        this.p_batters_faced = p_batters_faced;
        this.p_home_runs = p_homer_runs;
        this.p_walks = p_walks;
        this.p_strikeouts = p_strikeouts;
        this.p_games = p_games;
        this.p_runs = p_runs;
        this.p_gidp = p_gidp;
        this.p_sb = p_sb;
        this.p_hit_by_pitch = p_hit_by_pitch;
    }

    public int getP_losses() {
        return p_losses;
    }
    public int getP_wins() {
        return p_wins;
    }
    public float getP_era() {
        return p_era;
    }
    public String getP_ip() {
        return p_ip;
    }
    public int getP_hits() {
        return p_hits;
    }
    public int getP_earned_runs() {
        return p_earned_runs;
    }
    public float getP_whip() {
        return p_whip;
    }
    public int getP_batters_faced() {
        return p_batters_faced;
    }
    public int getP_homer_runs() {
        return p_home_runs;
    }
    public int getP_walks() {
        return p_walks;
    }
    public int getP_strikeouts() {
        return p_strikeouts;
    }
    public int getP_games() {
        return p_games;
    }
    public int getP_runs() {
        return p_runs;
    }
    public int getP_gidp() {
        return p_gidp;
    }
    public int getP_sb() {
        return p_sb;
    }
    public int getP_hit_by_pitch() {
        return p_hit_by_pitch;
    }

}
