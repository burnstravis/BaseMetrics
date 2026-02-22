package me.basemetrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    private int id; //primary key
    private String name;
    private String abbreviation;
    private String location;
    private String image_url;

    //columns in standings tables
    private String league;
    private String division;
    private int wins;
    private int losses;
    private Timestamp last_updated;
    private float games_back;

    @JsonCreator
    public Team(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("abbreviation") String abbreviation,
            @JsonProperty("locationName") String location,
            @JsonProperty("image_url") String image_url,
            @JsonProperty("league") JsonNode leagueNode,
            @JsonProperty("division") JsonNode divisionNode,
            @JsonProperty("wins") int wins,
            @JsonProperty("losses") int losses,
            @JsonProperty("last_updated") Timestamp last_updated,
            @JsonProperty("gamesBack") float games_back)
    {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
        this.location = location;

        this.image_url = (image_url != null) ? image_url :
                "https://www.mlbstatic.com/team-logos/" + id + ".svg";

        this.league = (leagueNode != null) ? leagueNode.path("name").asText() : "N/A";
        this.division = (divisionNode != null) ? divisionNode.path("name").asText() : "N/A";

        this.wins = wins;
        this.losses = losses;
        this.last_updated = last_updated;
        this.games_back = games_back;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getAbbreviation() {
        return abbreviation;
    }
    public String getLocation() {
        return location;
    }
    public String getImage_url() {
        return image_url;
    }
    public String getLeague() {
        return league;
    }
    public String getDivision() {
        return division;
    }
    public int getWins() {
        return wins;
    }
    public int getLosses() {
        return losses;
    }
    public Timestamp getLast_updated() {
        return last_updated;
    }
    public float getGames_back() {
        return games_back;
    }

}
