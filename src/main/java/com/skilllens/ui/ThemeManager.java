package com.skilllens.ui;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages modern desktop theming via AtlantaFX (Cupertino Dark / Light).
 */
public class ThemeManager {

    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    private static boolean isDarkMode = true;

    public static void applyInitialTheme() {
        setTheme(true);
    }

    public static void setTheme(boolean darkMode) {
        isDarkMode = darkMode;
        try {
            if (darkMode) {
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
            } else {
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            }
            log.info("Applied desktop theme: {}", darkMode ? "Cupertino Dark" : "Cupertino Light");
        } catch (Exception e) {
            log.warn("Theme application notice: {}", e.getMessage());
        }
    }

    public static void toggleTheme() {
        setTheme(!isDarkMode);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}
