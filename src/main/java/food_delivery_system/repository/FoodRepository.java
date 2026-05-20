package food_delivery_system.repository;

import food_delivery_system.model.Food;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

// @Repository indicates this class is part of Persistence Layer (DAO layer)
// Repository pattern: handles all database/file operations for Food entity
@Repository
public class FoodRepository {

    // File used as a simple database (file-based persistence)
    private static final String FILE = "foods.txt";

    // Dependency Injection using @Autowired
    // FileUtil handles file read/write operations (abstraction of file handling)
    @Autowired private FileUtil fileUtil;

    // read operations

    // Retrieves all food records from file
    public List<Food> findAll() {

        // Reads all lines from file and converts each line into Food object
        return fileUtil.readAllLines(FILE).stream()

                // Ignore empty lines
                .filter(l -> !l.isBlank())

                // Convert each line into Food object (mapping)
                .map(this::parse)

                // Collect results into List
                .collect(Collectors.toList());
    }

    // Finds food by ID
    public Food findById(String id) {

        // Stream filtering to find matching food item
        return findAll().stream()

                // Filter by food ID
                .filter(f -> f.getId().equals(id))

                // Return first match or null
                .findFirst().orElse(null);
    }

    // Finds foods belonging to a specific restaurant
    public List<Food> findByRestaurant(String rid) {

        return findAll().stream()

                // Filter by restaurant ID
                .filter(f -> rid.equals(f.getRestaurantId()))

                .collect(Collectors.toList());
    }

    // Search foods by name or category
    public List<Food> search(String q) {

        // Convert query to lowercase for case-insensitive search
        String s = q == null ? "" : q.toLowerCase();

        return findAll().stream()

                // Search in name or category fields
                .filter(f -> f.getName().toLowerCase().contains(s)
                        || (f.getCategory()!=null && f.getCategory().toLowerCase().contains(s)))

                .collect(Collectors.toList());
    }
    // Saves new food item into file
    public Food save(Food f) {

        // Generates ID if not already set
        if (f.getId() == null || f.getId().isBlank())
            f.setId("F-" + FileUtil.nextId());

        // Writes new record into file (append operation)
        fileUtil.appendLine(FILE, toLine(f));

        return f;
    }

    public void update(Food f) {

        // Reads all records and replaces matching record
        List<String> lines = findAll().stream()

                // Replace old record with updated object
                .map(x -> toLine(x.getId().equals(f.getId()) ? f : x))

                .collect(Collectors.toList());

        // Rewrite entire file (file-based DB update strategy)
        fileUtil.writeAllLines(FILE, lines);
    }

    // Deletes food by ID
    public void delete(String id) {

        List<String> lines = findAll().stream()

                // Remove matching food item
                .filter(f -> !f.getId().equals(id))

                .map(this::toLine)

                .collect(Collectors.toList());

        fileUtil.writeAllLines(FILE, lines);
    }

    // Deletes all foods of a restaurant
    public void deleteByRestaurant(String rid) {

        List<String> lines = findAll().stream()

                // Remove all foods of that restaurant
                .filter(f -> !rid.equals(f.getRestaurantId()))

                .map(this::toLine)

                .collect(Collectors.toList());

        fileUtil.writeAllLines(FILE, lines);
    }

    // Converts Food object → String (for file storage)
    private String toLine(Food f) {

        return FileUtil.join(
                f.getId(),
                f.getRestaurantId(),
                f.getName(),
                f.getCategory(),
                f.getPrice(),
                f.getImage(),
                f.getDescription()
        );
    }

    // Converts String → Food object (parsing file data)
    private Food parse(String l) {

        String[] p = FileUtil.split(l);

        double price = 0;

        try {
            price = Double.parseDouble(g(p,4));
        } catch (Exception ignored) {
            // Exception handling for invalid number format
        }

        return new Food(
                g(p,0), g(p,1), g(p,2), g(p,3),
                price, g(p,5), g(p,6)
        );
    }

    // Safe getter method for array index access
    private static String g(String[] a, int i){
        return i < a.length ? a[i] : "";
    }

}