package me.basemetrics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;

@Entity
@Table(name = "player_bios")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@com.fasterxml.jackson.annotation.JsonRootName(value = "people")
public class PlayerBio {

    @Id
    private int player_id;
    private String primaryNumber;
    private Date birthDate;
    private int currentAge;
    private String birthCity;
    private String birthCountry;
    private String height;
    private int weight;
    private boolean active;
    private String fullFMLName;

    protected PlayerBio() {}

    @JsonCreator
    public PlayerBio(@JsonProperty("id") int player_id,
                     @JsonProperty("primaryNumber") String primaryNumber,
                     @JsonProperty("birthDate") Date birthDate,
                     @JsonProperty("currentAge") int currentAge,
                     @JsonProperty("birthCity") String birthCity,
                     @JsonProperty("birthCountry") String birthCountry,
                     @JsonProperty("height") String height,
                     @JsonProperty("weight") int weight,
                     @JsonProperty("active") boolean active,
                     @JsonProperty("fullFMLName") String fullFMLName
                     )
    {
        this.player_id = player_id;
        this.primaryNumber = primaryNumber;
        this.birthDate = birthDate;
        this.currentAge = currentAge;
        this.birthCity = birthCity;
        this.birthCountry = birthCountry;
        this.height = height;
        this.weight = weight;
        this.active = active;
        this.fullFMLName = fullFMLName;

    }

    public int getPlayer_id() {
        return player_id;
    }
    public String getPrimaryNumber() {
        return primaryNumber;
    }
    public Date getBirthDate() {
        return birthDate;
    }
    public int getCurrentAge() {
        return currentAge;
    }
    public String getBirthCity() {
        return birthCity;
    }
    public String getBirthCountry() {
        return birthCountry;
    }
    public String getHeight() {
        return height;
    }
    public int getWeight() {
        return weight;
    }
    public boolean isActive() {
        return active;
    }
    public String getFullFMLName() {
        return fullFMLName;
    }

}
