package me.basemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class ApiConfig {
    @Bean
    public Semaphore mlbApiSemaphore() {
        return new Semaphore(20);
    }
}