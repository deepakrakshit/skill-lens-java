package com.skilllens;

import com.skilllens.ui.SkillLensDesktopApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

/**
 * Main application entry point for SkillLens.
 * Bootstraps Spring Boot context and launches the JavaFX desktop application.
 */
@SpringBootApplication
public class SkillLensApplication {

    private static final Logger log = LoggerFactory.getLogger(SkillLensApplication.class);
    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        log.info("Starting SkillLens Application...");

        boolean isHeadless = Arrays.asList(args).contains("--headless")
                || Arrays.asList(args).contains("--server-only")
                || "true".equalsIgnoreCase(System.getProperty("java.awt.headless"));

        SpringApplication app = new SpringApplication(SkillLensApplication.class);
        if (!isHeadless) {
            app.setHeadless(false);
        }

        applicationContext = app.run(args);
        log.info("Spring Boot context initialized successfully.");

        if (!isHeadless) {
            try {
                SkillLensDesktopApp.launchApp(applicationContext, args);
            } catch (Exception e) {
                log.warn("Desktop UI launch encountered warning: {}. Running in backend service mode.", e.getMessage());
            }
        }
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
