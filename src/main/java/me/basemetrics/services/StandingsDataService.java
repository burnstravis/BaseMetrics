package me.basemetrics.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StandingsDataService {

    public static String getStandingsUrl(){

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = now.format(formatter);
        String year = String.valueOf(now.getYear());

        return "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season=" + year + "&standingsTypes=regularSeason&date=" + date;
    }


}
