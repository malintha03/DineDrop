package food_delivery_system.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileUtil utility class
 * Utility/Helper class used for file handling operations
 * Abstraction concept:
 * Hides low level file I/O details from repositories and services
 *
 * All repositories use this class for reading/writing .txt files
 * Uses pipe-delimited records for data storage
 */
@Component
public class FileUtil {

    // Reads value from application.properties
    // Default directory = "data" if property not found
    @Value("${foodiego.data.dir:data}")
    private String dataDir;

    // Regex delimiter used for splitting pipe-separated values
    public static final String DELIM = "\\|";

    // Actual delimiter character used in text files
    public static final String DELIM_CHAR = "|";

    // Returns file path for given file name
    public Path filePath(String fileName) {

        // Path object represents directory path
        Path dir = Paths.get(dataDir);

        try {

            // Creates directory if it doesn't exist
            if (!Files.exists(dir))
                Files.createDirectories(dir);

        } catch (IOException ignored) {}

        // Resolve combines directory path with file name
        Path p = dir.resolve(fileName);

        // Creates file if file does not exist
        if (!Files.exists(p)) {

            try {

                Files.createFile(p);

            } catch (IOException ignored) {}
        }

        return p;
    }

    /**
     * Reads all lines from file
     * synchronized keyword used for thread safety
     * Prevents multiple threads from accessing method at same time
     */
    public synchronized List<String> readAllLines(String fileName) {

        try {

            // Reads all file lines into List collection
            return Files.readAllLines(filePath(fileName));

        } catch (IOException e) {

            // Returns empty list if file reading fails
            return new ArrayList<>();
        }
    }

    /**
     * Appends single line into file
     * synchronized ensures safe concurrent access
     */
    public synchronized void appendLine(String fileName, String line) {

        // try-with-resources automatically closes BufferedWriter
        try (BufferedWriter w = Files.newBufferedWriter(filePath(fileName),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE)) {

            // Writes line into file
            w.write(line);

            // Moves to next line
            w.newLine();

        } catch (IOException e) {

            // Converts checked exception into runtime exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Overwrites entire file with new data
     * synchronized for thread-safe file access
     */
    public synchronized void writeAllLines(String fileName, List<String> lines) {

        try {

            // Replaces old content with new content
            Files.write(filePath(fileName), lines,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);

        } catch (IOException e) {

            // Exception handling
            throw new RuntimeException(e);
        }
    }

    /**
     * Escapes special characters before saving to file
     * Prevents formatting issues in text storage
     */
    public static String esc(String v) {

        // Null handling
        if (v == null)
            return "";

        // Replaces pipe symbol and line breaks
        return v.replace("|", "/")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    // Joins multiple values into single pipe-delimited string
    public static String join(Object... parts) {

        // StringBuilder improves performance when building strings
        StringBuilder sb = new StringBuilder();

        // Loop through all objects
        for (int i = 0; i < parts.length; i++) {

            // Adds delimiter after first value
            if (i > 0)
                sb.append(DELIM_CHAR);

            // Converts object into escaped string
            sb.append(esc(parts[i] == null ? "" : parts[i].toString()));
        }

        return sb.toString();
    }

    // Splits line into array using delimiter
    public static String[] split(String line) {

        // -1 keeps empty values in array
        return line.split(DELIM, -1);
    }

    /**
     * Generates unique numeric ID
     * Uses current system time + random number
     */
    public static String nextId() {

        // System.currentTimeMillis() gives current timestamp
        return Long.toString(System.currentTimeMillis())
                + (int)(Math.random() * 1000);
    }

    // OOP Concepts Used:
    // 1. Abstraction -> Hides file operations from repositories
    // 2. Encapsulation -> Private dataDir variable
    // 3. Composition -> Repositories use FileUtil object
    // 4. Utility class design using static methods

    // Thread Operations:
    // synchronized keyword prevents multiple threads
    // from modifying files simultaneously

    // File Handling Concepts:
    // 1. BufferedWriter
    // 2. Files API
    // 3. Path and Paths classes
    // 4. Try-with-resources
    // 5. Exception handling

    // SOLID Principles:
    // Single Responsibility Principle:
    // Handles only file-related operations

    // Reusability:
    // Same utility class reused by multiple repositories

}