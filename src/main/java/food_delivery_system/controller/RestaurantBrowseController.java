package food_delivery_system.controller;

import food_delivery_system.model.*;
import food_delivery_system.repository.UserRepository;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class RestaurantBrowseController {

    @Autowired private RestaurantService restaurantService;
    @Autowired private FoodService foodService;
    @Autowired private OrderService orderService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/restaurants")
    public String browseRestaurants(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        String customerCity = currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole())
                ? (currentUser.getCity() == null ? "" : currentUser.getCity().trim()) : "";

        List<Restaurant> restaurants = restaurantService.all();
        if (!customerCity.isBlank()) {
            restaurants = restaurants.stream()
                    .filter(r -> customerCity.equalsIgnoreCase(r.getCity() == null ? "" : r.getCity().trim()))
                    .toList();
        }

        model.addAttribute("restaurants", restaurants);
        model.addAttribute("customerCity", customerCity);
        model.addAttribute("reviewService", reviewService);
        return "restaurants";
    }

    @GetMapping("/restaurants/{id}")
    public String restaurantProfile(@PathVariable String id, HttpSession session, Model model) {
        Restaurant restaurant = restaurantService.byId(id);
        if (restaurant == null) return "redirect:/restaurants";

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole())
                && currentUser.getCity() != null && !currentUser.getCity().isBlank()
                && !currentUser.getCity().trim().equalsIgnoreCase(restaurant.getCity() == null ? "" : restaurant.getCity().trim())) {
            return "redirect:/restaurants";
        }

        List<Food> foods = foodService.byRestaurant(id);
        List<Order> orders = orderService.byRestaurant(id);
        User owner = userRepository.findById(restaurant.getOwnerId());

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("foods", foods);
        model.addAttribute("owner", owner);
        model.addAttribute("orders", orders);
        model.addAttribute("reviews", reviewService.all().stream()
                .filter(rv -> orders.stream().anyMatch(o -> o.getId().equals(rv.getOrderId())))
                .toList());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("completedOrders", orders.stream().filter(Order::isCompleted).count());
        model.addAttribute("activeOrders", orders.stream().filter(Order::isActive).count());
        return "restaurant-profile";
    }

    @GetMapping("/admin/restaurants/{id}")
    public String adminRestaurantProfile(@PathVariable String id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) return "redirect:/admin-login";
        return restaurantProfile(id, session, model);
    }
}
