package me.basemetrics.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DontevConfig {

    static {
        String mode = System.getProperty("env.mode", "local");
        String filename = ".env." + mode;

        Dotenv dotenv = Dotenv.configure()
                .directory("./envs")
                .filename(filename)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        System.out.println("Environment loaded from: " + filename);
    }
}