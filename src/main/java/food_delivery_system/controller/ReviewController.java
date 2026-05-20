package food_delivery_system.controller;

import food_delivery_system.model.*;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// @Controller indicates this class is a Spring MVC Controller
// Controller layer handles user requests and responses
@Controller
public class ReviewController {

    // Dependency Injection using @Autowired
    // Spring automatically creates and injects service objects

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    // Handles GET request to open review form
    @GetMapping("/review/add/{orderId}")
    public String form(@PathVariable String orderId, HttpSession session, Model model) {

        // Getting logged-in user from session
        User u = (User) session.getAttribute("user");

        // Session validation
        // Redirect user if not logged in
        if (u == null) return "redirect:/login";

        // Fetch order using service layer
        // Abstraction used because controller accesses data through service methods
        Order o = orderService.byId(orderId);

        // Validation check
        // Customer can only review delivered orders
        if (o == null || !"DELIVERED".equals(o.getStatus()))
            return "redirect:/customer/orders";

        // Sending data to frontend view page
        model.addAttribute("order", o);

        // Aggregation relationship:
        // Order is connected with Restaurant using restaurantId
        model.addAttribute("restaurant", restaurantService.byId(o.getRestaurantId()));

        // Returns review form page
        return "add-review";
    }

    // Handles POST request when submitting review
    @PostMapping("/review/add/{orderId}")
    public String submit(@PathVariable String orderId,
                         @RequestParam int rating,
                         @RequestParam String comment,
                         HttpSession session) {

        // Get logged-in user
        User u = (User) session.getAttribute("user");

        // Authentication check
        if (u == null) return "redirect:/login";

        // Find order details
        Order o = orderService.byId(orderId);

        // Validation check
        if (o == null) return "redirect:/customer/orders";

        // Creating Review object using constructor
        // Object-Oriented Programming concept
        // Encapsulation used through private variables and getters/setters
        Review r = new Review(
                null,
                u.getId(),
                u.getName(),
                o.getRestaurantId(),
                o.getId(),
                rating,
                comment,
                ""
        );

        // Save review using service layer
        reviewService.add(r);

        // Redirect after successful submission
        return "redirect:/reviews";
    }

    // Handles viewing reviews
    @GetMapping("/reviews")
    public String myReviews(HttpSession session, Model model) {

        // Get current logged-in user
        User u = (User) session.getAttribute("user");

        // Login validation
        if (u == null) return "redirect:/login";

        // Polymorphism concept:
        // Different behavior depending on user role
        if ("CUSTOMER".equalsIgnoreCase(u.getRole())) {

            // Customer sees only own reviews
            model.addAttribute("reviews", reviewService.byCustomer(u.getId()));

        } else {

            // Admin or other roles see all reviews
            model.addAttribute("reviews", reviewService.all());
        }

        // Sending restaurant service to view
        model.addAttribute("restaurantService", restaurantService);

        // Return reviews page
        return "view-reviews";
    }

    // Handles opening edit review form
    @GetMapping("/review/edit/{id}")
    public String editForm(@PathVariable String id,
                           HttpSession session,
                           Model model) {

        // Get logged-in user
        User u = (User) session.getAttribute("user");

        // Login validation
        if (u == null) return "redirect:/login";

        // Find review by ID
        Review r = reviewService.byId(id);

        // Authorization check
        // User can edit only their own review
        if (r == null || !r.getCustomerId().equals(u.getId()))
            return "redirect:/reviews";

        // Send review data to frontend
        model.addAttribute("review", r);

        // Send restaurant details
        model.addAttribute("restaurant",
                restaurantService.byId(r.getRestaurantId()));

        // Return edit page
        return "edit-review";
    }

    // Handles updating edited review
    @PostMapping("/review/edit/{id}")
    public String editSubmit(@PathVariable String id,
                             @RequestParam int rating,
                             @RequestParam String comment,
                             HttpSession session) {

        // Get logged-in user
        User u = (User) session.getAttribute("user");

        // Authentication validation
        if (u == null) return "redirect:/login";

        // Fetch review
        Review r = reviewService.byId(id);

        // Authorization validation
        if (r == null || !r.getCustomerId().equals(u.getId()))
            return "redirect:/reviews";

        // Encapsulation:
        // Updating values using setter methods
        r.setRating(rating);
        r.setComment(comment);

        // Save updated review
        reviewService.update(r);

        // Redirect after update
        return "redirect:/reviews";
    }

    // Handles deleting review
    @PostMapping("/review/delete/{id}")
    public String customerDelete(@PathVariable String id,
                                 HttpSession session) {

        // Get logged-in user
        User u = (User) session.getAttribute("user");

        // Authentication validation
        if (u == null) return "redirect:/login";

        // Find review by ID
        Review r = reviewService.byId(id);

        // Authorization check before deleting
        if (r != null && r.getCustomerId().equals(u.getId())) {

            // Delete operation using service layer
            // Repository pattern may be used inside service/repository classes
            reviewService.delete(id);
        }

        // Redirect after delete
        return "redirect:/reviews";
    }
}