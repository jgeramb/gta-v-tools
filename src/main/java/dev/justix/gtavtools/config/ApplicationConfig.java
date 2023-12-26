package dev.justix.gtavtools.config;

public class ApplicationConfig {

    public static final JsonConfig CONFIG;

    static {
        CONFIG = new JsonConfig("config.json");

        Runtime.getRuntime().addShutdownHook(new Thread(CONFIG::save));
    }

}
