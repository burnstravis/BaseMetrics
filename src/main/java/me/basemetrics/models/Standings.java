package me.basemetrics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "standings")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Standings {

    @Id
    private int team_id;

    private String league;
    private String division;
    private String team;
    private int wins;
    private int losses;
    private Timestamp last_updated;
    private float games_back;

    @Column(name = "wild_card_games_back")
    private float wildCardGamesBack;
    @Column(name = "league_games_back")
    private float leagueGamesBack;

    @Column(name = "streak_code")
    private String streakCode;

    @Column(name = "home_wins")
    private int homeWins;

    @Column(name = "home_losses")
    private int homeLosses;

    @Column(name = "away_wins")
    private int awayWins;

    @Column(name = "away_losses")
    private int awayLosses;

    @Column(name = "last_ten_wins")
    private int lastTenWins;

    @Column(name = "last_ten_lossess")
    private int lastTenLosses;

    @Column(name = "above500_wins")
    private int above500Wins;
    @Column(name = "above500_losses")
    private int above500Losses;

    @Column(name = "expected_wins")
    private int expectedWins;
    @Column(name = "expected_losses")
    private int expectedLosses;

    protected Standings() {}

    @JsonCreator
    public Standings(
            @JsonProperty("team") JsonNode teamNode,
            @JsonProperty("league") JsonNode leagueNode,
            @JsonProperty("division") JsonNode divisionNode,
            @JsonProperty("name") String team,
            @JsonProperty("wins") int wins,
            @JsonProperty("losses") int losses,
            @JsonProperty("last_updated") Timestamp last_updated,
            @JsonProperty("gamesBack") float games_back,
            @JsonProperty("wildCardGamesBack") float wildCardGamesBack,
            @JsonProperty("leagueGamesBack") float leagueGamesBack,
            @JsonProperty("streak") JsonNode streakNode,
            @JsonProperty("records") JsonNode recordsNode
    )
    {
        this.team_id = (teamNode != null) ? teamNode.path("id").asInt() : 0;
        this.team = team;
        this.league = (leagueNode != null) ? leagueNode.path("name").asText() : "N/A";
        this.division = (divisionNode != null) ? divisionNode.path("name").asText() : "N/A";

        this.wins = wins;
        this.losses = losses;
        this.last_updated = last_updated;
        this.games_back = games_back;
        this.wildCardGamesBack = wildCardGamesBack;
        this.leagueGamesBack = leagueGamesBack;

        if(streakNode != null) {
            this.streakCode = streakNode.path("streakCode").asText();
        }


        if (recordsNode != null && recordsNode.has("splitRecords")) {
            JsonNode splits = recordsNode.get("splitRecords");

            if (splits.has(0)) {
                this.homeWins = splits.get(0).path("wins").asInt();
                this.homeLosses = splits.get(0).path("losses").asInt();
            }

            if (splits.has(1)) {
                this.awayWins = splits.get(1).path("wins").asInt();
                this.awayLosses = splits.get(1).path("losses").asInt();
            }

            if (splits.has(8)) {
                this.lastTenWins = splits.get(8).path("wins").asInt();
                this.lastTenLosses = splits.get(8).path("losses").asInt();
            }

            if (splits.has(11)) {
                this.above500Wins = splits.get(11).path("wins").asInt();
                this.above500Losses = splits.get(11).path("losses").asInt();
            }
        }

        if (recordsNode != null && recordsNode.has("expectedRecords")) {
            JsonNode xRecord = recordsNode.get("expectedRecords").get(0);
            this.expectedWins = xRecord.path("wins").asInt();
            this.expectedLosses = xRecord.path("losses").asInt();
        }
    }

    public int getTeam_id() { return team_id; }
    public String getLeague() { return league; }
    public String getDivision() { return division; }
    public String getTeamName(){return team; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public Timestamp getLast_updated() { return last_updated; }
    public float getGames_back() { return games_back; }
    public String getTeam() {
        return team;
    }
    public int getExpectedLosses() {
        return expectedLosses;
    }
    public int getExpectedWins() {
        return expectedWins;
    }
    public int getAbove500Losses() {
        return above500Losses;
    }
    public int getAbove500Wins() {
        return above500Wins;
    }
    public int getLastTenLosses() {
        return lastTenLosses;
    }
    public int getLastTenWins() {
        return lastTenWins;
    }
    public int getAwayLosses() {
        return awayLosses;
    }
    public int getAwayWins() {
        return awayWins;
    }
    public int getHomeLosses() {
        return homeLosses;
    }
    public int getHomeWins() {
        return homeWins;
    }
    public String getStreakCode() {
        return streakCode;
    }
    public float getLeagueGamesBack() {
        return leagueGamesBack;
    }
    public float getWildCardGamesBack() {
        return wildCardGamesBack;
    }

}