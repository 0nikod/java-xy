package com.campus.secondhand.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {

    private static final String DEFAULT_APP_TITLE = "校园二手教材与物品交易平台";
    private static final String DEFAULT_DB_PATH = "data/secondhand.db";

    private AppConfig() {
    }

    public static String getAppTitle() {
        return DEFAULT_APP_TITLE;
    }

    public static Path resolveDatabasePath() {
        String configuredPath = System.getProperty("SECONDHAND_DB_PATH");
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            configuredPath = System.getenv("SECONDHAND_DB_PATH");
        }
        Path path;
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            path = Paths.get(DEFAULT_DB_PATH);
        } else {
            path = Paths.get(configuredPath.trim());
        }
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
    }

    public static String getDatabaseUrl() {
        return "jdbc:sqlite:" + resolveDatabasePath().toString();
    }

    public static String getOpenAiApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            return "";
        }
        return apiKey.trim();
    }

    public static boolean isOpenAiConfigured() {
        return !getOpenAiApiKey().isEmpty();
    }
}
