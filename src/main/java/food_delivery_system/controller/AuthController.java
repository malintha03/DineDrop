package food_delivery_system.controller;

import food_delivery_system.model.User;
import food_delivery_system.service.AuthService;
import food_delivery_system.service.RestaurantService;
import food_delivery_system.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// @Controller marks this class as a Spring MVC Controller
// Controller layer handles HTTP requests and returns views/pages
// MVC Architecture: This is the Controller component
@Controller
public class AuthController {

    // Dependency Injection using @Autowired
    // AuthService contains business logic related to authentication
    @Autowired private AuthService authService;

    // Service class used to fetch restaurant data
    @Autowired private RestaurantService restaurantService;

    // Service class used to fetch review data
    @Autowired private ReviewService reviewService;

    // Handles home page request
    // Model is used to send data from controller to view
    @GetMapping("/")
    public String home(Model model) {

        // Fetch all restaurants from service layer
        model.addAttribute("restaurants", restaurantService.all());

        // Fetch all reviews from service layer
        model.addAttribute("reviews", reviewService.all());

        // Returns home.html view
        return "home";
    }

    // Displays login page
    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String role, Model model) {

        // Adds role data to frontend
        model.addAttribute("role", role == null ? "" : role);

        return "login";
    }

    // Handles login form submission
    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password,
                          HttpSession session, Model model) {

        // Calls service layer to validate login
        User u = authService.login(email, password);

        // Basic validation and error handling
        if (u == null) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }

        // Session used to store logged-in user details
        session.setAttribute("user", u);

        // Polymorphism concept can appear in service implementations
        // Redirect user based on role
        return "redirect:" + dashboardFor(u.getRole());
    }

    // Shows admin login page
    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    // Handles admin login functionality
    @PostMapping("/admin-login")
    public String adminLoginPost(@RequestParam String email, @RequestParam String password,
                                 HttpSession session, Model model) {

        // Service layer authentication
        User u = authService.login(email, password);

        // Role validation for admin access
        if (u == null || !"ADMIN".equalsIgnoreCase(u.getRole())) {

            // Error message passed to frontend
            model.addAttribute("error", "Invalid admin credentials");

            return "admin-login";
        }

        // Store admin object in session
        session.setAttribute("user", u);

        return "redirect:/admin";
    }

    // Displays registration page
    @GetMapping("/register")
    public String registerPage(@RequestParam(required=false) String role, Model model) {

        // Converts role to uppercase for consistency
        model.addAttribute("role", role == null ? "CUSTOMER" : role.toUpperCase());

        return "register";
    }

    // Handles user registration
    @PostMapping("/register")
    public String doRegister(@RequestParam String name, @RequestParam String email,
                             @RequestParam String password, @RequestParam String phone,
                             @RequestParam String role,
                             @RequestParam(required=false) String city,
                             @RequestParam(required=false) String vehicle,
                             @RequestParam(required=false) String licenseNumber,
                             @RequestParam(required=false) String licensePlate,
                             HttpSession session, Model model) {

        // Object creation using constructor
        // Encapsulation used through private fields inside User class
        User u = new User(null, name, email, password, phone, role.toUpperCase(),
                city == null ? "" : city, vehicle == null ? "" : vehicle,
                licenseNumber == null ? "" : licenseNumber,
                licensePlate == null ? "" : licensePlate);

        // Calls service layer to register user
        String err = authService.register(u);

        // Validation and exception-like handling
        if (err != null) {

            model.addAttribute("error", err);
            model.addAttribute("role", role);

            return "register";
        }

        // Store registered user in session
        session.setAttribute("user", u);

        return "redirect:" + dashboardFor(u.getRole());
    }

    // Logout functionality
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // Invalidates current session
        session.invalidate();

        return "redirect:/";
    }

    // Displays profile page
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        // Type casting session object to User
        User u = (User) session.getAttribute("user");

        // Authentication check
        if (u == null) return "redirect:/login";

        // Send user object to frontend
        model.addAttribute("user", u);

        return "profile";
    }

    // Handles profile update
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name, @RequestParam String phone,
                                @RequestParam(required=false) String city,
                                @RequestParam(required=false) String vehicle,
                                @RequestParam(required=false) String licenseNumber,
                                @RequestParam(required=false) String licensePlate,
                                @RequestParam(required=false) String password,
                                HttpSession session, Model model) {

        // Retrieve logged-in user from session
        User u = (User) session.getAttribute("user");

        if (u == null) return "redirect:/login";

        // Encapsulation using setter methods
        u.setName(name);
        u.setPhone(phone);

        if (city != null) u.setCity(city);

        if (vehicle != null) u.setVehicle(vehicle);

        // Conditional logic for rider role
        if ("RIDER".equalsIgnoreCase(u.getRole())) {

            if (licenseNumber != null) u.setLicenseNumber(licenseNumber);

            if (licensePlate != null) u.setLicensePlate(licensePlate);

            // Validation method from service layer
            String err = authService.validateRiderFields(u);

            if (err != null) {

                model.addAttribute("user", u);
                model.addAttribute("error", err);

                return "profile";
            }
        }

        // Password updated only if not blank
        if (password != null && !password.isBlank()) {
            u.setPassword(password);
        }

        // Repository pattern is used internally inside service classes
        // Update user data through AdminService
        adminUpdate(u);

        // Update session data
        session.setAttribute("user", u);

        return "redirect:/profile?saved=1";
    }

    // Handles profile deletion
    @PostMapping("/profile/delete")
    public String deleteProfile(HttpSession session) {

        User u = (User) session.getAttribute("user");

        if (u == null) return "redirect:/login";

        // Deletes user using service layer
        adminDelete(u.getId());

        // Clears session after deletion
        session.invalidate();

        return "redirect:/";
    }

    // Dependency Injection for AdminService
    @Autowired private food_delivery_system.service.AdminService adminService;

    // Helper method to update user
    private void adminUpdate(User u){

        // Service layer communicates with repository/database layer
        adminService.updateUser(u);
    }

    // Helper method to delete user
    private void adminDelete(String id){

        // CRUD delete operation
        adminService.deleteUser(id);
    }

    // Method decides dashboard URL based on user role
    // Switch expression introduced in newer Java versions
    private String dashboardFor(String role) {

        return switch (role.toUpperCase()) {

            case "OWNER" -> "/owner";

            case "RIDER" -> "/rider";

            case "ADMIN" -> "/admin";

            default -> "/customer";
        };
    }
}