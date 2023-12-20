package dev.justix.gtavtools.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final OutputStream out;

    public Logger(OutputStream out) {
        this.out = out;
    }

    public void log(Level level, String message) {
        writeToStream(((System.out == out) && level.equals(Level.SEVERE)) ? System.err : out, "[" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + " " + level.name() + ": " + Thread.currentThread().getName() + "] " + message + System.lineSeparator());
    }

    public void writeToStream(OutputStream outputStream, String message) {
        try {
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            System.err.println("Could not write to stream: " + ex.getMessage());
        }
    }

}
