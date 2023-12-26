package dev.justix.gtavtools.config;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonConfig {

    private final File file;
    private JSONObject data;

    public JsonConfig(String fileName) {
        this.file = new File(fileName);

        if(!file.exists()) {
            InputStream defaultFile = JsonConfig.class.getResourceAsStream("/" + fileName);

            try {
                if(defaultFile != null)
                    Files.copy(defaultFile, this.file.toPath());
                else if (!this.file.createNewFile())
                    Logger.getGlobal().log(Level.WARNING, "Could not create configuration file");
            } catch (IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, String.format("Failed to create configuration file: %s", ex.getMessage()));
            }
        }

        this.data = getData();
    }

    public void save() {
        try(FileWriter fileWriter = new FileWriter(this.file)) {
            fileWriter.write(this.data.toString(2));
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to save configuration file: %s", ex.getMessage()));
        }
    }

    private JSONObject getData() {
        if(this.data != null)
            return this.data;

        try {
            String fileContents = Files.readString(this.file.toPath());

            if(fileContents != null) {
                JSONObject jsonObject = new JSONObject(fileContents);

                return (this.data = jsonObject);
            }
        } catch (Exception ignore) {
            this.data = new JSONObject();
        }

        return this.data;
    }

    public void set(String key, Object value) {
        this.data.put(key, value);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

}
