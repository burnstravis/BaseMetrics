package me.basemetrics;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Dotenv dotenv;

    private static synchronized void loadDotenv() {
        if (dotenv == null) {
            String mode = System.getProperty("env.mode", "local");
            String fileName = ".env." + mode;

            System.out.println("Loading config: ./envs/" + fileName);

            dotenv = Dotenv.configure()
                    .directory("./envs")
                    .filename(fileName)
                    .load();
        }
    }

    public static Connection getConnection() throws SQLException {
        loadDotenv();

        String host = dotenv.get("DB_HOST");
        String port = dotenv.get("DB_PORT");
        String name = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String pass = dotenv.get("DB_PASS");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?rewriteBatchedStatements=true";

        System.out.println("Connecting to: " + url + " as " + user);

        return DriverManager.getConnection(url, user, pass);
    }
}