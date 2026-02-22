package me.basemetrics;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

public class PlayerDataRunner {

    private final TeamDataService teamService = new TeamDataService();
    private final PlayerDataService playerService = new PlayerDataService();


    public void syncTeam(int teamId) {

        System.out.println("Starting sync for team: " + teamId);
        try {

            List<Integer> playerIds = teamService.fetchRosterIds(teamId); //list of teams player id's

            List<Player> players;
            try (var scope = StructuredTaskScope.open()) {

                //List of subtasks (for each player in teamId)
                List<StructuredTaskScope.Subtask<Player>> tasks = playerIds.stream()
                        .map(id -> scope.fork(() -> playerService.fetchPlayerData(id)))
                        .toList();

                scope.join();

                //Save player objects back to List<Player> players
                players = tasks.stream()
                        .map(StructuredTaskScope.Subtask::get)
                        .toList();
            }

            //Connect to DataBase
            try (Connection conn = Database.getConnection()) {
                //Save batch of player data on foreign key teamId
                playerService.savePlayersBatch(players, conn, teamId);
            }

            System.out.println("Successfully synced " + players.size() + " players for team " + teamId);

        } catch (Exception e) {
            System.err.println("Failed to sync team " + teamId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String envMode = (args.length > 0) ? args[0] : "local";
        System.setProperty("env.mode", envMode);

        //Instance of Player runner
        PlayerDataRunner runner = new PlayerDataRunner();
        int[] teamIds = {108, 109, 110, 111, 112, 113, 114,
                        115, 116, 117, 118, 119, 120, 121,
                        133, 134, 135, 136, 137, 138, 139,
                        140, 141, 142, 143, 144, 145, 146,
                        147, 158
        }; //158 159 for AL NL all-stars


        //Each Team
        for (int id : teamIds) {
            runner.syncTeam(id); //Run player data for each team
        }
    }
}