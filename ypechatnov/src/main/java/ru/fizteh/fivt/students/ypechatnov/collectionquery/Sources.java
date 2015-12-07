package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Helper methods to create collections.
 *
 * @author akormushin
 */
public class Sources {

    /**
     * @param items
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> List<T> list(T... items) {
        return Arrays.asList(items);
    }

    /**
     * @param items
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> Set<T> set(T... items) {
        return new HashSet<T>(list(items));
    }

    /**
     * @param inputStream
     * @param <T>
     * @return
     */
    public static <T> Stream<T> lines(InputStream inputStream) throws NoSuchMethodException {
        // I can't get instances without Class<?>
        // wrong template
        throw new NoSuchMethodException();
    }

    /**
     * @param file
     * @param <T>
     * @return
     */
    public static <T> Stream<T> lines(Path file) throws IOException, NoSuchMethodException {
        try (FileInputStream input = new FileInputStream(file.toFile())) {
            return lines((InputStream) input);
        }
    }

}
