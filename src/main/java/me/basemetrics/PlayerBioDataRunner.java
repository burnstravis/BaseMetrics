package me.basemetrics;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

public class PlayerBioDataRunner {

    private final PlayerBioDataService playerBioDataService = new PlayerBioDataService();

    public void syncAllPlayerBios() {
        System.out.println("Starting Player Bio Sync...");

        try {
            List<Integer> playerIds = playerBioDataService.getAllPlayerIds();
            System.out.println("Found " + playerIds.size() + " players to sync.");

            List<PlayerBio> bios;

            try (var scope = StructuredTaskScope.open(Joiner.<PlayerBio>allSuccessfulOrThrow())) {

                System.out.println("Fetching bios from MLB API...");

                playerIds.forEach(id -> scope.fork(() -> playerBioDataService.fetchPlayerBioData(id)));

                bios = scope.join()
                        .map(StructuredTaskScope.Subtask::get)
                        .filter(java.util.Objects::nonNull)
                        .toList();
            }

            System.out.println("Saving " + bios.size() + " bios to database...");
            try (Connection conn = Database.getConnection()) {
                playerBioDataService.savePlayerBioBatch(bios, conn);
            }

            System.out.println("Sync Complete!");

        } catch (Throwable e) {
            System.err.println("Critical failure during bio sync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String envMode = (args.length > 0) ? args[0] : "local";
        System.setProperty("env.mode", envMode);

        PlayerBioDataRunner runner = new PlayerBioDataRunner();
        runner.syncAllPlayerBios();
    }
}