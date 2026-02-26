package me.basemetrics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
public class LiveGameRunner {

    private static final LiveGameService liveGameService = new LiveGameService();

    public static void main(String[] args) {

        String envMode = (args.length > 0) ? args[0] : "local";
        System.setProperty("env.mode", envMode);

        long start = System.nanoTime();

        try {
            List<Integer> liveGameIds = liveGameService.getAllLiveGameIds(LiveGameService.TODAY);
            liveGameIds.addAll(liveGameService.getAllLiveGameIds(LiveGameService.YESTERDAY));

            System.out.println("All Games today and Ongoing Games from yesterday: " + liveGameIds.size());

            List<LiveGame> games;

            try (var scope = StructuredTaskScope.open()) {

                List<StructuredTaskScope.Subtask<LiveGame>> tasks = liveGameIds.stream()
                        .map(gameId -> scope.fork(() -> liveGameService.fetchLiveGameData(gameId)))
                        .toList();

                scope.join();

                games = tasks.stream()
                        .map(StructuredTaskScope.Subtask::get)
                        .toList();

                saveData(games);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long end = System.nanoTime();
        double durationSeconds = (end - start) / 1_000_000_000.0;
        System.out.printf("Execution finished in: %.3f seconds%n", durationSeconds);
    }

    private static void saveData(List<LiveGame> liveGames) {
        try (Connection conn = Database.getConnection()) {
            liveGameService.saveLiveGameBatch(liveGames, conn);
            System.out.println("Successfully saved " + liveGames.size() + " Live Games.");
        } catch (SQLException ex) {
            throw new RuntimeException("Database error during batch save", ex);
        }
    }

}
