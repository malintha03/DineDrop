package food_delivery_system.repository;

import food_delivery_system.model.Review;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

// @Repository marks this class as Repository layer
// Repository layer handles data access and storage operations
@Repository
public class ReviewRepository {

    // File name used to store review records
    // File handling concept
    private static final String FILE = "reviews.txt";

    // Dependency Injection using @Autowired
    // Spring automatically injects FileUtil object
    @Autowired
    private FileUtil fileUtil;

    // Retrieve all review records
    public List<Review> findAll() {

        // Read all lines from file
        // Stream API used for processing collections
        return fileUtil.readAllLines(FILE)
                .stream()

                // Ignore empty lines
                .filter(l -> !l.isBlank())

                // Convert text lines into Review objects
                .map(this::parse)

                // Convert stream into List
                .collect(Collectors.toList());
    }

    // Save new review record
    public Review save(Review r) {

        // Generate ID if ID is empty
        if (r.getId() == null || r.getId().isBlank())

            // Static method used for unique ID generation
            r.setId("RV-" + FileUtil.nextId());

        // Append review data into file
        fileUtil.appendLine(FILE, toLine(r));

        return r;
    }

    // Update existing review
    public Review update(Review updated) {

        // Get all existing reviews
        List<Review> all = findAll();

        // Replace old review with updated review
        List<String> lines = all.stream().map(r -> {

            // Check matching ID
            if (r.getId().equals(updated.getId()))
                return toLine(updated);

            return toLine(r);

        }).collect(Collectors.toList());

        // Rewrite file with updated data
        fileUtil.writeAllLines(FILE, lines);

        return updated;
    }

    // Find review using ID
    public Review findById(String id) {

        // findFirst() returns first matching object
        return findAll()
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Find reviews by customer ID
    public List<Review> findByCustomer(String cid) {

        return findAll()
                .stream()
                .filter(r -> cid.equals(r.getCustomerId()))
                .collect(Collectors.toList());
    }

    // Find reviews by restaurant ID
    public List<Review> findByRestaurant(String rid) {

        return findAll()
                .stream()
                .filter(r -> rid.equals(r.getRestaurantId()))
                .collect(Collectors.toList());
    }

    // Delete review record
    public void delete(String id) {

        // Create new list excluding deleted review
        List<String> lines = findAll()
                .stream()
                .filter(r -> !r.getId().equals(id))
                .map(this::toLine)
                .collect(Collectors.toList());

        // Rewrite file after deletion
        fileUtil.writeAllLines(FILE, lines);
    }

    // Convert Review object into text format
    private String toLine(Review r) {

        // Abstraction:
        // File conversion details hidden inside method
        return FileUtil.join(
                r.getId(),
                r.getCustomerId(),
                r.getCustomerName(),
                r.getRestaurantId(),
                r.getOrderId(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }

    // Convert file line into Review object
    private Review parse(String l) {

        // Split text line into array
        String[] p = FileUtil.split(l);

        int rating = 0;

        try {

            // Convert String into integer
            rating = Integer.parseInt(g(p,5));

        } catch (Exception ignored) {

            // Exception handling:
            // Prevents application crash if conversion fails
        }

        // Create Review object using parsed data
        return new Review(
                g(p,0),
                g(p,1),
                g(p,2),
                g(p,3),
                g(p,4),
                rating,
                g(p,6),
                g(p,7)
        );
    }

    // Helper method for safe array access
    private static String g(String[] a, int i) {

        // Prevents ArrayIndexOutOfBoundsException
        return i < a.length ? a[i] : "";
    }
}