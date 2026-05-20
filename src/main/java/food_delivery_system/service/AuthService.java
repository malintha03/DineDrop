package food_delivery_system.service;

import food_delivery_system.model.User;
import food_delivery_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service layer in MVC architecture
 * Handles authentication and registration business logic
 * Service layer acts between Controller and Repository layers
 */
@Service
public class AuthService {

    // Dependency Injection using @Autowired
    // Repository object used for database/file operations
    @Autowired
    private UserRepository userRepo;

    /**
     * Regular Expression for Sri Lankan NIC validation
     * Supports:
     * 1. New NIC format -> 12 digits
     * 2. Old NIC format -> 9 digits followed by V or X
     */
    public static final String NIC_NUMBER_REGEX = "^(\\d{12}|\\d{9}[VX])$";

    /**
     * Regular Expression for vehicle number validation
     * Example formats:
     * ABC-1234
     * WP-CAB-1234
     */
    public static final String VEHICLE_NUMBER_REGEX = "^([A-Z]{2,3}-)?[A-Z]{2,3}-\\d{4}$";

    // Login method for user authentication
    public User login(String email, String password) {

        // Fetch user using email
        User u = userRepo.findByEmail(email);

        // Checks whether user exists and password matches
        if (u != null && u.getPassword().equals(password))

            // Returns logged-in user object
            return u;

        // Returns null if login fails
        return null;
    }

    // Handles user registration
    public String register(User u) {

        // Validation for empty email
        if (u.getEmail()==null || u.getEmail().isBlank())
            return "Email required";

        // Password validation
        if (u.getPassword()==null || u.getPassword().length() < 4)
            return "Password must be at least 4 chars";

        // Checks duplicate email registration
        if (userRepo.findByEmail(u.getEmail()) != null)
            return "Email already registered";

        // Default role assignment
        if (u.getRole() == null || u.getRole().isBlank())
            u.setRole("CUSTOMER");

        // Rider-specific validation
        if ("RIDER".equalsIgnoreCase(u.getRole())) {

            // Calls separate validation method
            String err = validateRiderFields(u);

            // Returns validation error if exists
            if (err != null)
                return err;
        }

        // Save user using repository layer
        userRepo.save(u);

        // null means registration successful
        return null;
    }

    /**
     * Validates rider registration fields
     * Returns error message if validation fails
     * Returns null if validation passes
     */
    public String validateRiderFields(User u) {

        // Null handling + trim removes unwanted spaces
        String city = u.getCity() == null ? "" : u.getCity().trim();

        String vehicle = u.getVehicle() == null ? "" : u.getVehicle().trim();

        // Converts NIC to uppercase for consistency
        String nic = u.getLicenseNumber() == null ? "" :
                u.getLicenseNumber().trim().toUpperCase();

        // Converts vehicle number to uppercase
        String vehicleNumber = u.getLicensePlate() == null ? "" :
                u.getLicensePlate().trim().toUpperCase();

        // Validation for city
        if (city.isBlank())
            return "Service city is required for rider registration";

        // Validation for vehicle type
        if (vehicle.isBlank())
            return "Vehicle type is required for rider registration";

        // Regular expression validation for NIC
        if (!nic.matches(NIC_NUMBER_REGEX))

            return "ID / NIC number must be 12 digits or old NIC format: 9 digits followed by V or X";

        // Regular expression validation for vehicle number
        if (!vehicleNumber.matches(VEHICLE_NUMBER_REGEX))

            return "Vehicle number must use ABC-1234 or WP-CAB-1234 format";

        // Encapsulation using setter methods
        // Stores cleaned/formatted values back into object
        u.setCity(city);
        u.setVehicle(vehicle);
        u.setLicenseNumber(nic);
        u.setLicensePlate(vehicleNumber);

        // Validation successful
        return null;
    }

    // OOP Concepts Used:
    // 1. Encapsulation -> User object fields accessed through getters/setters
    // 2. Abstraction -> Service layer hides business logic from controller
    // 3. Composition -> AuthService uses UserRepository object
    // 4. Polymorphism may occur if interfaces/services are extended later

    // SOLID Principles:
    // Single Responsibility Principle:
    // This class only handles authentication and registration logic

    // Dependency Inversion Principle:
    // Service depends on repository layer instead of direct file handling

    // MVC Architecture:
    // Controller -> AuthController
    // Service -> AuthService
    // Repository -> UserRepository
    // Model -> User

    // Exception/Validation Handling:
    // Instead of crashing program, validation errors return messages safely

}