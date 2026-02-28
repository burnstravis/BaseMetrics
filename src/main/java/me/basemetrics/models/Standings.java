package me.basemetrics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
            @JsonProperty("gamesBack") float games_back
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
    }

    public int getTeam_id() { return team_id; }
    public String getLeague() { return league; }
    public String getDivision() { return division; }
    public String getTeamName(){return team; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public Timestamp getLast_updated() { return last_updated; }
    public float getGames_back() { return games_back; }
}