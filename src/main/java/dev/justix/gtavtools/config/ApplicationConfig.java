package dev.justix.gtavtools.config;

public class ApplicationConfig {

    public static final JsonConfig CONFIG = new JsonConfig("config.json");

    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(CONFIG::save));
    }

}
