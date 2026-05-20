package food_delivery_system.controller;

import food_delivery_system.model.*;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

// @Controller annotation marks this class as a Spring MVC Controller handles HTTP requests and responses in MVC architecture
@Controller
public class FoodController {

    // Dependency Injection using @Autowired
    // Service layer object used to handle food-related business logic
    @Autowired private FoodService foodService;

    // Dependency Injection for restaurant operations
    @Autowired private RestaurantService restaurantService;

    // Reads upload directory path from application.properties
    // @Value annotation injects configuration value
    @Value("${foodiego.uploads.dir:uploads}") private String uploadsDir;

    // Private helper method used for authorization checking
    // Encapsulation: internal validation logic hidden inside controller
    private User requireOwner(HttpSession session) {

        // Session object stores logged-in user details
        User u = (User) session.getAttribute("user");

        // Checks whether user exists and has OWNER role
        if (u == null || !"OWNER".equalsIgnoreCase(u.getRole())) return null;

        return u;
    }

    // Helper method checks restaurant ownership
    // Relationship: aggregation between User and Restaurant
    private boolean ownsRestaurant(User owner, Restaurant restaurant) {

        // Validates owner and restaurant using object comparison
        return owner != null && restaurant != null && owner.getId().equals(restaurant.getOwnerId());
    }

    // Helper method checks whether owner owns the food item
    private boolean ownsFood(User owner, Food food) {

        // Defensive programming for null safety
        if (owner == null || food == null) return false;

        // Service layer abstraction used instead of direct database access
        Restaurant restaurant = restaurantService.byId(food.getRestaurantId());

        return ownsRestaurant(owner, restaurant);
    }

    // Handles GET request for viewing foods
    // @GetMapping maps HTTP GET request to this method
    @GetMapping("/foods")
    public String viewFoods(@RequestParam(required=false) String q,
                            @RequestParam(required=false) String restaurantId,
                            @RequestParam(required=false) String category,
                            HttpSession session, Model model) {

        User currentUser = (User) session.getAttribute("user");
        String customerCity = currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole())
                ? (currentUser.getCity() == null ? "" : currentUser.getCity().trim()) : "";

        List<Restaurant> visibleRestaurants = restaurantService.all();
        if (!customerCity.isBlank()) {
            visibleRestaurants = visibleRestaurants.stream()
                    .filter(r -> customerCity.equalsIgnoreCase(r.getCity() == null ? "" : r.getCity().trim()))
                    .toList();
        }
        Set<String> visibleRestaurantIds = new HashSet<>();
        for (Restaurant r : visibleRestaurants) visibleRestaurantIds.add(r.getId());

        List<Food> foods;

        // Checks whether foods are filtered by restaurant
        if (restaurantId != null && !restaurantId.isBlank()) {

            Restaurant selectedRestaurant = restaurantService.byId(restaurantId);
            if (!customerCity.isBlank() && (selectedRestaurant == null
                    || !customerCity.equalsIgnoreCase(selectedRestaurant.getCity() == null ? "" : selectedRestaurant.getCity().trim()))) {
                foods = java.util.Collections.emptyList();
                selectedRestaurant = null;
            } else {
                foods = foodService.byRestaurant(restaurantId);
            }

            model.addAttribute("restaurant", selectedRestaurant);

        } else if (q != null) {

            // Search functionality using service layer
            foods = foodService.search(q);

            // Sends search query back to frontend
            model.addAttribute("q", q);

        } else {

            // Retrieves all food items
            foods = foodService.all();
        }

        if (!visibleRestaurantIds.isEmpty()) {
            foods = foods.stream().filter(f -> visibleRestaurantIds.contains(f.getRestaurantId())).toList();
        } else if (!customerCity.isBlank()) {
            foods = java.util.Collections.emptyList();
        }

        List<String> categories = foodService.all().stream()
                .filter(f -> visibleRestaurantIds.isEmpty() || visibleRestaurantIds.contains(f.getRestaurantId()))
                .map(Food::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        if (category != null && !category.isBlank()) {
            String selectedCategory = category.trim();
            foods = foods.stream()
                    .filter(f -> selectedCategory.equalsIgnoreCase(f.getCategory() == null ? "" : f.getCategory().trim()))
                    .toList();
            model.addAttribute("category", selectedCategory);
        }

        model.addAttribute("foods", foods);
        model.addAttribute("categories", categories);
        model.addAttribute("customerCity", customerCity);

        // Sends restaurant service to Thymeleaf view
        model.addAttribute("restaurantService", restaurantService);

        // Returns view page name
        return "view-foods";
    }

    // Displays add food form
    @GetMapping("/owner/food/add/{restaurantId}")
    public String addFood(@PathVariable String restaurantId, HttpSession session, Model model) {

        // Authorization check
        User u = requireOwner(session);

        // Redirects unauthenticated users
        if (u == null) return "redirect:/login";

        // Retrieves restaurant object
        Restaurant r = restaurantService.byId(restaurantId);

        // Prevents unauthorized restaurant access
        if (!ownsRestaurant(u, r)) return "redirect:/owner";

        // Sends restaurant details to frontend
        model.addAttribute("restaurant", r);

        return "add-food";
    }

    // Handles form submission for adding food
    // @PostMapping handles HTTP POST request
    @PostMapping("/owner/food/add/{restaurantId}")
    public String addFoodPost(@PathVariable String restaurantId,
                              @RequestParam String name, @RequestParam String category,
                              @RequestParam double price, @RequestParam String description,
                              @RequestParam(required=false) MultipartFile image,
                              HttpSession session) {

        User u = requireOwner(session);

        if (u == null) return "redirect:/login";

        Restaurant r = restaurantService.byId(restaurantId);

        if (!ownsRestaurant(u, r)) return "redirect:/owner";

        // File handling operation for image upload
        String img = saveImage(image);

        // Object creation using constructor
        // Encapsulation: Food object stores related data together
        foodService.add(new Food(null, restaurantId, name, category, price, img, description));

        return "redirect:/owner";
    }

    // Displays edit food page
    @GetMapping("/owner/food/edit/{id}")
    public String editFood(@PathVariable String id, HttpSession session, Model model) {

        User u = requireOwner(session);

        if (u == null) return "redirect:/login";

        // Retrieves food object using abstraction through service layer
        Food f = foodService.byId(id);

        // Authorization validation
        if (!ownsFood(u, f)) return "redirect:/owner";

        // Sends food object to frontend
        model.addAttribute("f", f);

        // Retrieves related restaurant
        model.addAttribute("restaurant", restaurantService.byId(f.getRestaurantId()));

        return "edit-food";
    }

    // Handles update operation for food item
    @PostMapping("/owner/food/edit/{id}")
    public String editFoodPost(@PathVariable String id,
                               @RequestParam String name, @RequestParam String category,
                               @RequestParam double price, @RequestParam String description,
                               @RequestParam(required=false) MultipartFile image,
                               HttpSession session) {

        User u = requireOwner(session);

        if (u == null) return "redirect:/login";

        Food f = foodService.byId(id);

        if (!ownsFood(u, f)) return "redirect:/owner";

        // Setter methods demonstrate encapsulation
        f.setName(name);
        f.setCategory(category);
        f.setPrice(price);
        f.setDescription(description);

        // Saves uploaded image
        String img = saveImage(image);

        // Updates image only if new image exists
        if (img != null) f.setImage(img);

        // Service layer update operation
        foodService.update(f);

        return "redirect:/owner";
    }

    // Handles food deletion
    @PostMapping("/owner/food/delete/{id}")
    public String deleteFood(@PathVariable String id, HttpSession session) {

        User u = requireOwner(session);

        if (u == null) return "redirect:/login";

        Food f = foodService.byId(id);

        // Deletes food only if owner has permission
        if (ownsFood(u, f)) foodService.delete(id);

        return "redirect:/owner";
    }

    // Helper method for image upload handling
    // Abstraction: image saving logic separated into reusable method
    private String saveImage(MultipartFile image) {

        // Validation check for empty image
        if (image == null || image.isEmpty()) return null;

        try {

            // Path object represents upload directory
            Path dir = Paths.get(uploadsDir);

            // Creates directory if not exists
            if (!Files.exists(dir)) Files.createDirectories(dir);

            // Gets original file name
            String original = image.getOriginalFilename() == null ? "image" : image.getOriginalFilename();

            // Generates unique file name using current time
            String fn = System.currentTimeMillis() + "_" + original.replaceAll("\\s+","_");

            // Copies uploaded file into uploads directory
            Files.copy(image.getInputStream(), dir.resolve(fn), StandardCopyOption.REPLACE_EXISTING);

            return fn;

        } catch (IOException e) {

            // Exception handling for file operations
            return null;
        }
    }

}