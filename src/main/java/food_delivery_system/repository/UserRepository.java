package food_delivery_system.repository;

import food_delivery_system.model.User;
import food_delivery_system.util.FileUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository layer in MVC architecture
 * Repository pattern is used to handle data storage operations
 * This class manages user data using users.txt file
 * Stores Customer, Owner, Rider, and Admin details
 */
@Repository
public class UserRepository {

    // File name used for storing user records
    private static final String FILE = "users.txt";

    // Dependency Injection using @Autowired
    // FileUtil handles file reading/writing operations
    @Autowired
    private FileUtil fileUtil;

    // @PostConstruct runs automatically after bean creation
    // Used here to create default admin account
    @PostConstruct
    public void seed() {

        // Checks whether admin already exists
        if (findByEmail("admin@dinedrop.com") == null) {

            // Saves default admin user into file
            save(new User("U-ADMIN", "Admin", "admin@dinedrop.com", "admin123",
                    "0000000000", "ADMIN", "HQ", "", "", ""));
        }
    }

    // Returns all users from users.txt
    public List<User> findAll() {

        // Stream API used for processing collections
        // File handling operation
        return fileUtil.readAllLines(FILE).stream()

                // Removes blank lines
                .filter(l -> !l.isBlank())

                // Converts text line into User object
                .map(this::parse)

                // Converts stream into List collection
                .collect(Collectors.toList());
    }

    // Finds user using ID
    public User findById(String id) {

        // Stream filtering operation
        return findAll().stream()

                // Lambda expression used
                .filter(u -> u.getId().equals(id))

                // Returns first matching user
                .findFirst()

                // Returns null if not found
                .orElse(null);
    }

    // Finds user using email
    public User findByEmail(String email) {

        // equalsIgnoreCase avoids case-sensitive matching
        return findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // Returns users based on role
    public List<User> findByRole(String role) {

        // Collection filtering using Stream API
        return findAll().stream()
                .filter(u -> u.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    // Saves new user into file
    public User save(User u) {

        // Generates ID if ID is empty
        if (u.getId() == null || u.getId().isBlank()) {

            // Static utility method used
            u.setId("U-" + FileUtil.nextId());
        }

        // Appends user data into file
        // File handling operation
        fileUtil.appendLine(FILE, toLine(u));

        return u;
    }

    // Updates existing user data
    public void update(User u) {

        // ArrayList collection used
        List<User> all = findAll();

        // Stores updated file lines
        List<String> lines = new ArrayList<>();

        // Loop through all users
        for (User x : all)

            // Ternary operator used
            // Replaces matching user with updated object
            lines.add(toLine(x.getId().equals(u.getId()) ? u : x));

        // Rewrites entire file with updated data
        fileUtil.writeAllLines(FILE, lines);
    }

    // Deletes user from file
    public void delete(String id) {

        // Removes matching user by filtering
        List<String> lines = findAll().stream()

                // Keeps users except deleted one
                .filter(u -> !u.getId().equals(id))

                // Converts User object back to text
                .map(this::toLine)

                .collect(Collectors.toList());

        // Saves updated records to file
        fileUtil.writeAllLines(FILE, lines);
    }

    // Converts User object into text line format
    // Serialization-like process for file storage
    private String toLine(User u) {

        // join() utility method combines values into single line
        return FileUtil.join(u.getId(), u.getName(), u.getEmail(), u.getPassword(),
                u.getPhone(), u.getRole(), u.getCity(), u.getVehicle(),

                // Null checking to avoid errors
                u.getLicenseNumber() == null ? "" : u.getLicenseNumber(),
                u.getLicensePlate() == null ? "" : u.getLicensePlate());
    }

    // Converts text line into User object
    // Deserialization-like process
    private User parse(String line) {

        // Splits line into array values
        String[] p = FileUtil.split(line);

        // Creates User object using parsed data
        return new User(get(p,0), get(p,1), get(p,2), get(p,3), get(p,4), get(p,5),
                get(p,6), get(p,7), get(p,8), get(p,9));
    }

    // Helper method for safe array access
    private static String get(String[] a, int i){

        // Prevents ArrayIndexOutOfBoundsException
        return i<a.length ? a[i] : "";
    }

    // OOP Concepts Used:
    // 1. Encapsulation -> User object uses private variables with getters/setters
    // 2. Abstraction -> Repository hides file handling implementation
    // 3. Dependency Injection -> @Autowired FileUtil
    // 4. Composition -> UserRepository uses FileUtil object

    // SOLID Principles:
    // Single Responsibility Principle:
    // This class only handles user repository operations


}