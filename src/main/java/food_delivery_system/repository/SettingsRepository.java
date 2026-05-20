package food_delivery_system.repository;

import food_delivery_system.model.Settings;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository class for system-wide settings.
 * This is a SINGLE-ROW FILE REPOSITORY (only one settings record exists).
 * Format: restaurantCommissionPct|riderCommissionPct
 *
 * Repository Pattern: Handles persistence logic (file read/write).
 */
@Repository
public class SettingsRepository {

    // File acting as simple database for system settings
    private static final String FILE = "settings.txt";

    // Dependency Injection using Spring
    // FileUtil abstracts file handling operations
    @Autowired private FileUtil fileUtil;

    // Load

    public Settings load() {

        // Reads all lines from settings file
        List<String> lines = fileUtil.readAllLines(FILE);

        // Loop through file lines (usually only one line exists)
        for (String l : lines) {

            // Skip empty or blank lines
            if (l == null || l.isBlank()) continue;

            // Split line into parts using FileUtil helper
            String[] p = FileUtil.split(l);

            try {
                // Parse restaurant commission percentage
                double rPct = p.length > 0 ? Double.parseDouble(p[0]) : 5.0;

                // Parse rider commission percentage
                double riderPct = p.length > 1 ? Double.parseDouble(p[1]) : 10.0;

                // Return Settings object (Model instantiation)
                return new Settings(rPct, riderPct);

            } catch (Exception ignored) {

                // Exception handling for invalid file data
                // If parsing fails, default settings will be used
            }
        }

        // If file is empty or invalid, create default settings
        Settings s = new Settings();

        // Save default settings back to file
        save(s);

        return s;
    }

    // save

    public void save(Settings s) {

        // Writes only ONE LINE (single-row configuration)
        fileUtil.writeAllLines(FILE, java.util.List.of(

                // Convert Settings object → file format string
                FileUtil.join(
                        s.getRestaurantCommissionPct(),
                        s.getRiderCommissionPct()
                )
        ));
    }

}