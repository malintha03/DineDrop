package food_delivery_system.controller;

// Importing models, services, and Spring classes
import food_delivery_system.model.*;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // Marks this class as a Spring MVC Controller
public class AdminController {

    // Injecting required services
    @Autowired private AdminService adminService;
    @Autowired private RestaurantService restaurantService;
    @Autowired private OrderService orderService;
    @Autowired private FoodService foodService;
    @Autowired private RevenueService revenueService;
    @Autowired private ReviewService reviewService;
    @Autowired private PaymentService paymentService;

    // Injecting user repository
    @Autowired private food_delivery_system.repository.UserRepository userRepository;

    // Method to check whether logged user is ADMIN
    private User requireAdmin(HttpSession s) {

        // Get logged user from session
        User u = (User) s.getAttribute("user");

        // If no user OR role is not ADMIN return null
        if (u == null || !"ADMIN".equalsIgnoreCase(u.getRole()))
            return null;

        // Otherwise return admin user
        return u;
    }

    //Admin Dashboard

    @GetMapping("/admin")
    public String dashboard(HttpSession s, Model m) {

        // Only admin can access
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Sending dashboard statistics to frontend
        m.addAttribute("usersCount", adminService.allUsers().size());
        m.addAttribute("restaurantsCount", restaurantService.all().size());
        m.addAttribute("ordersCount", orderService.all().size());
        m.addAttribute("foodsCount", foodService.all().size());
        m.addAttribute("reviewsCount", reviewService.all().size());
        m.addAttribute("paymentsCount", paymentService.all().size());

        // Revenue statistics
        m.addAttribute("revenueOverall", revenueService.overall());
        m.addAttribute("revenueToday", revenueService.today());
        m.addAttribute("revenueMonth", revenueService.thisMonth());

        // Open admin-dashboard.html
        return "admin-dashboard";
    }

    // ================= MANAGE USERS =================

    @GetMapping("/admin/users")
    public String users(HttpSession s, Model m) {

        // Check admin access
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Get all users
        java.util.List<User> all = adminService.allUsers();

        // Send all users
        m.addAttribute("users", all);

        // Filter users by role
        m.addAttribute("customers",
                all.stream()
                        .filter(u -> "CUSTOMER".equalsIgnoreCase(u.getRole()))
                        .toList());

        m.addAttribute("owners",
                all.stream()
                        .filter(u -> "OWNER".equalsIgnoreCase(u.getRole()))
                        .toList());

        m.addAttribute("riders",
                all.stream()
                        .filter(u -> "RIDER".equalsIgnoreCase(u.getRole()))
                        .toList());

        m.addAttribute("admins",
                all.stream()
                        .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                        .toList());

        // Open manage-users.html
        return "manage-users";
    }

    // Delete user
    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable String id,
                             HttpSession s,
                             RedirectAttributes ra) {

        // Get admin
        User admin = requireAdmin(s);

        if (admin == null)
            return "redirect:/admin-login";

        // Prevent admin deleting own account
        if (admin.getId().equals(id)) {

            ra.addFlashAttribute("error",
                    "You cannot delete your own admin account.");

            return "redirect:/admin/users";
        }

        // Delete user
        adminService.deleteUser(id);

        // Success message
        ra.addFlashAttribute("success", "User deleted.");

        return "redirect:/admin/users";
    }

    //Manage Foods

    @GetMapping("/admin/foods")
    public String adminFoods(HttpSession s, Model m) {

        // Admin authentication
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Send foods list
        m.addAttribute("foods", foodService.all());

        // Send restaurant service to frontend
        m.addAttribute("restaurantService", restaurantService);

        return "manage-foods";
    }

    // Delete food item
    @PostMapping("/admin/foods/delete/{id}")
    public String adminDeleteFood(@PathVariable String id,
                                  HttpSession s,
                                  RedirectAttributes ra) {

        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Delete food
        foodService.delete(id);

        // Success message
        ra.addFlashAttribute("success", "Food deleted.");

        return "redirect:/admin/foods";
    }

    //Manage Restaurants

    @GetMapping("/admin/restaurants")
    public String restaurants(HttpSession s, Model m) {

        // Check admin
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Send all restaurants
        m.addAttribute("restaurants", restaurantService.all());

        return "manage-restaurants";
    }

    // Delete restaurant
    @PostMapping("/admin/restaurants/delete/{id}")
    public String deleteRestaurant(@PathVariable String id,
                                   HttpSession s) {

        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Delete restaurant
        restaurantService.delete(id);

        return "redirect:/admin/restaurants";
    }

    //Manage Orders

    @GetMapping("/admin/orders")
    public String orders(HttpSession s, Model m) {

        // Check admin
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Send all orders
        m.addAttribute("orders", orderService.all());

        // Send restaurant service
        m.addAttribute("restaurantService", restaurantService);

        return "manage-orders";
    }

    // Delete order
    @PostMapping("/admin/orders/delete/{id}")
    public String deleteOrder(@PathVariable String id,
                              HttpSession s) {

        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Delete order
        orderService.delete(id);

        return "redirect:/admin/orders";
    }

    // ================= DELETE REVIEWS =================

    @PostMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable String id,
                               HttpSession s) {

        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Delete review
        reviewService.delete(id);

        return "redirect:/reviews";
    }

    // ====================================================
    // ================= RIDER SECTION ====================
    // ====================================================

    @GetMapping("/rider")
    public String riderDashboard(HttpSession s, Model m) {

        // Get logged rider
        User u = (User) s.getAttribute("user");

        // Only RIDER can access
        if (u == null || !"RIDER".equalsIgnoreCase(u.getRole()))
            return "redirect:/login";

        // Get rider orders
        java.util.List<Order> myOrders =
                orderService.byRider(u.getId());

        // Rider city
        final String riderCity =
                u.getCity() == null ? "" : u.getCity().trim();

        m.addAttribute("riderCity", riderCity);

        // Available orders for rider
        m.addAttribute("available",
                orderService.unassigned().stream()

                        // Only PREPARING orders
                        .filter(o -> "PREPARING"
                                .equalsIgnoreCase(o.getStatus()))

                        // Only same city orders
                        .filter(o -> riderCity.equalsIgnoreCase(
                                o.getCity() == null ? ""
                                        : o.getCity().trim()))

                        .toList());

        // Current active orders
        m.addAttribute("mine",
                myOrders.stream()
                        .filter(o -> !"DELIVERED"
                                .equalsIgnoreCase(o.getStatus())
                                &&
                                !"CANCELLED"
                                        .equalsIgnoreCase(o.getStatus()))
                        .toList());

        // Completed orders
        m.addAttribute("completed",
                myOrders.stream()
                        .filter(o -> "DELIVERED"
                                .equalsIgnoreCase(o.getStatus()))
                        .toList());

        // Rider earnings
        m.addAttribute("payoutToday",
                orderService.riderPayoutToday(u.getId()));

        m.addAttribute("payoutMonth",
                orderService.riderPayoutMonth(u.getId()));

        m.addAttribute("payoutTotal",
                orderService.riderPayoutTotal(u.getId()));

        // Send services to frontend
        m.addAttribute("restaurantService", restaurantService);
        m.addAttribute("userRepository", userRepository);

        return "rider-dashboard";
    }

    // Rider accepts order
    @PostMapping("/rider/pick/{orderId}")
    public String pick(@PathVariable String orderId,
                       HttpSession s,
                       RedirectAttributes ra) {

        // Get rider
        User u = (User) s.getAttribute("user");

        if (u == null || !"RIDER".equalsIgnoreCase(u.getRole()))
            return "redirect:/login";

        // Find order
        Order order = orderService.byId(orderId);

        // Order not found
        if (order == null) {

            ra.addFlashAttribute("error", "Order not found.");

            return "redirect:/rider";
        }

        // Prevent multiple riders accepting same order
        if (order.getRiderId() != null
                && !order.getRiderId().isBlank()) {

            ra.addFlashAttribute("error",
                    "This order has already been picked by another rider.");

            return "redirect:/rider";
        }

        // Check city match
        String riderCity =
                u.getCity() == null ? "" : u.getCity().trim();

        String orderCity =
                order.getCity() == null ? "" : order.getCity().trim();

        if (!riderCity.equalsIgnoreCase(orderCity)) {

            ra.addFlashAttribute("error",
                    "You can only accept orders in your city ("
                            + riderCity + ").");

            return "redirect:/rider";
        }

        // Assign rider to order
        orderService.assignRider(orderId, u.getId());

        ra.addFlashAttribute("success", "Order accepted.");

        return "redirect:/rider";
    }

    // Rider updates order status
    @PostMapping("/rider/status/{orderId}")
    public String riderStatus(@PathVariable String orderId,
                              @RequestParam String status,
                              HttpSession s) {

        // Validate rider
        User u = (User) s.getAttribute("user");

        if (u == null || !"RIDER".equalsIgnoreCase(u.getRole()))
            return "redirect:/login";

        boolean ok = orderService.updateStatusByRider(orderId, u.getId(), status);
        if (!ok) {
            // Rider is only allowed to move own orders to OUT_FOR_DELIVERY or DELIVERED.
        }

        return "redirect:/rider";
    }

    // ====================================================
    // ================= OWNER SECTION ====================
    // ====================================================

    @PostMapping("/owner/order/status/{orderId}")
    public String ownerStatus(@PathVariable String orderId,
                              @RequestParam String status,
                              HttpSession s,
                              RedirectAttributes ra) {

        // Get logged owner
        User u = (User) s.getAttribute("user");

        // Only OWNER allowed
        if (u == null || !"OWNER".equalsIgnoreCase(u.getRole()))
            return "redirect:/login";

        java.util.List<String> ownerRestaurantIds = restaurantService.byOwner(u.getId())
                .stream().map(Restaurant::getId).toList();

        boolean allowed = orderService.updateStatusByOwner(orderId, u.getId(), ownerRestaurantIds, status);

        if (!allowed) {
            ra.addFlashAttribute("statusError",
                    "Restaurant owners can only update restaurant-side statuses (PENDING, PREPARING, or CANCELLED while pending). Only riders can mark Out for Delivery or Delivered.");
        }

        return "redirect:/owner";
    }

}