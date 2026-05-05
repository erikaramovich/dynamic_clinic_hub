//package com.miro.project.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Slf4j
//@Configuration
//public class FlywayConfig {
//
//    @Bean
//    public FlywayMigrationStrategy flywayMigrationStrategy() {
//        return flyway -> {
//            log.info("🚀 Forcing Flyway Migration to start...");
////            // repair() fixes the history table if it was left in a bad state previously
////            flyway.repair();
////            // migrate() executes the actual V1 script
////            flyway.migrate();
//            log.info("✅ Flyway Migration finished!");
//        };
//    }
//}