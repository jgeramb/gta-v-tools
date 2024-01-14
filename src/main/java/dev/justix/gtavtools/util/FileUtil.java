package dev.justix.gtavtools.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Objects;

public class FileUtil {

    public static boolean copyFromJar(String source, Path destination) {
        final URI sourcePath;

        try {
            sourcePath = Objects.requireNonNull(FileUtil.class.getResource(source)).toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Failed to load resource ", ex);
        }

        if(sourcePath.getScheme().equals("file"))
            return copyDirectory(Paths.get(sourcePath), destination);

        try(FileSystem jarFileSystem = FileSystems.newFileSystem(sourcePath, Collections.emptyMap())) {
            final Path jarPath = jarFileSystem.provider().getPath(sourcePath);

            return copyDirectory(jarPath, destination);
        } catch (IOException ignore) {
            return false;
        }
    }

    public static boolean copyDirectory(Path source, Path destination) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final Path target = destination.resolve(source.relativize(dir).toString());

                    Files.createDirectories(target);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path target = destination.resolve(source.relativize(file).toString());

                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignore) {
            return false;
        }

        return true;
    }

}
