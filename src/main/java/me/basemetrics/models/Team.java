package me.basemetrics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    @Id
    private int id; //primary key
    private String name;
    private String abbreviation;
    private String location;
    private String image_url;

    protected Team() {}

    @JsonCreator
    public Team(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("abbreviation") String abbreviation,
            @JsonProperty("locationName") String location,
            @JsonProperty("image_url") String image_url
            )
    {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
        this.location = location;

        this.image_url = (image_url != null) ? image_url :
                "https://www.mlbstatic.com/team-logos/" + id + ".svg";

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


}
