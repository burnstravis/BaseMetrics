package me.basemetrics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

public class TeamDataRunner {

    private static final TeamDataService teamService = new TeamDataService();

    public static void main(String[] args) {

        String envMode = (args.length > 0) ? args[0] : "local";
        System.setProperty("env.mode", envMode);

        List<Integer> teamIds = TeamDataService.getTeamIds();

        List<Team> teams;

        try (var scope = StructuredTaskScope.open()) {

            List<StructuredTaskScope.Subtask<Team>> tasks = teamIds.stream()
                    .map(id -> scope.fork(() -> teamService.fetchTeamData(id)))
                    .toList();

            scope.join();

            teams = tasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();

            saveData(teams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void saveData(List<Team> teams) {
        try (Connection conn = Database.getConnection()) {
            teamService.saveTeamBatch(teams, conn);
            System.out.println("Successfully saved " + teams.size() + " teams.");
        } catch (SQLException ex) {
            throw new RuntimeException("Database error during batch save", ex);
        }
    }

}