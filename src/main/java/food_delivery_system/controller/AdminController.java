package food_delivery_system.controller;

import food_delivery_system.model.*;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    @Autowired private AdminService adminService;
    @Autowired private RestaurantService restaurantService;
    @Autowired private OrderService orderService;
    @Autowired private FoodService foodService;
    @Autowired private RevenueService revenueService;
    @Autowired private ReviewService reviewService;
    @Autowired private PaymentService paymentService;
    @Autowired private food_delivery_system.repository.UserRepository userRepository;

    private User requireAdmin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"ADMIN".equalsIgnoreCase(u.getRole())) return null;
        return u;
    }

    @GetMapping("/admin")
    public String dashboard(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        m.addAttribute("usersCount", adminService.allUsers().size());
        m.addAttribute("restaurantsCount", restaurantService.all().size());
        m.addAttribute("ordersCount", orderService.all().size());
        m.addAttribute("foodsCount", foodService.all().size());
        m.addAttribute("reviewsCount", reviewService.all().size());
        m.addAttribute("paymentsCount", paymentService.all().size());
        m.addAttribute("revenueOverall", revenueService.overall());
        m.addAttribute("revenueToday", revenueService.today());
        m.addAttribute("revenueMonth", revenueService.thisMonth());
        return "admin-dashboard";
    }

    @GetMapping("/admin/users")
    public String users(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        java.util.List<User> all = adminService.allUsers();
        m.addAttribute("users", all);
        m.addAttribute("customers", all.stream().filter(u -> "CUSTOMER".equalsIgnoreCase(u.getRole())).toList());
        m.addAttribute("owners",    all.stream().filter(u -> "OWNER".equalsIgnoreCase(u.getRole())).toList());
        m.addAttribute("riders",    all.stream().filter(u -> "RIDER".equalsIgnoreCase(u.getRole())).toList());
        m.addAttribute("admins",    all.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).toList());
        return "manage-users";
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable String id, HttpSession s, RedirectAttributes ra) {
        User admin = requireAdmin(s);
        if (admin == null) return "redirect:/admin-login";
        if (admin.getId().equals(id)) {
            ra.addFlashAttribute("error", "You cannot delete your own admin account.");
            return "redirect:/admin/users";
        }
        adminService.deleteUser(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/foods")
    public String adminFoods(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        m.addAttribute("foods", foodService.all());
        m.addAttribute("restaurantService", restaurantService);
        return "manage-foods";
    }

    @PostMapping("/admin/foods/delete/{id}")
    public String adminDeleteFood(@PathVariable String id, HttpSession s, RedirectAttributes ra) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        foodService.delete(id);
        ra.addFlashAttribute("success", "Food deleted.");
        return "redirect:/admin/foods";
    }

    @GetMapping("/admin/restaurants")
    public String restaurants(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        m.addAttribute("restaurants", restaurantService.all());
        return "manage-restaurants";
    }

    @PostMapping("/admin/restaurants/delete/{id}")
    public String deleteRestaurant(@PathVariable String id, HttpSession s) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        restaurantService.delete(id);
        return "redirect:/admin/restaurants";
    }

    @GetMapping("/admin/orders")
    public String orders(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        m.addAttribute("orders", orderService.all());
        m.addAttribute("restaurantService", restaurantService);
        return "manage-orders";
    }

    @PostMapping("/admin/orders/delete/{id}")
    public String deleteOrder(@PathVariable String id, HttpSession s) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        orderService.delete(id);
        return "redirect:/admin/orders";
    }

    @PostMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable String id, HttpSession s) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        reviewService.delete(id);
        return "redirect:/reviews";
    }

    // ----- Rider area (kept here for brevity) -----
    @GetMapping("/rider")
    public String riderDashboard(HttpSession s, Model m) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"RIDER".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        java.util.List<Order> myOrders = orderService.byRider(u.getId());
        final String riderCity = u.getCity() == null ? "" : u.getCity().trim();
        m.addAttribute("riderCity", riderCity);
        m.addAttribute("available", orderService.unassigned().stream()
                .filter(o -> "PREPARING".equalsIgnoreCase(o.getStatus()))
                .filter(o -> riderCity.equalsIgnoreCase(o.getCity() == null ? "" : o.getCity().trim()))
                .toList());
        m.addAttribute("mine", myOrders.stream()
                .filter(o -> !"DELIVERED".equalsIgnoreCase(o.getStatus())
                        && !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .toList());
        m.addAttribute("completed", myOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .toList());
        m.addAttribute("payoutToday", orderService.riderPayoutToday(u.getId()));
        m.addAttribute("payoutMonth", orderService.riderPayoutMonth(u.getId()));
        m.addAttribute("payoutTotal", orderService.riderPayoutTotal(u.getId()));
        m.addAttribute("restaurantService", restaurantService);
        m.addAttribute("userRepository", userRepository);
        return "rider-dashboard";
    }

    @PostMapping("/rider/pick/{orderId}")
    public String pick(@PathVariable String orderId, HttpSession s, RedirectAttributes ra) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"RIDER".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        Order order = orderService.byId(orderId);
        if (order == null) {
            ra.addFlashAttribute("error", "Order not found.");
            return "redirect:/rider";
        }
        if (order.getRiderId() != null && !order.getRiderId().isBlank()) {
            ra.addFlashAttribute("error", "This order has already been picked by another rider.");
            return "redirect:/rider";
        }
        String riderCity = u.getCity() == null ? "" : u.getCity().trim();
        String orderCity = order.getCity() == null ? "" : order.getCity().trim();
        if (!riderCity.equalsIgnoreCase(orderCity)) {
            ra.addFlashAttribute("error", "You can only accept orders in your city (" + riderCity + ").");
            return "redirect:/rider";
        }
        orderService.assignRider(orderId, u.getId());
        ra.addFlashAttribute("success", "Order accepted.");
        return "redirect:/rider";
    }

    @PostMapping("/rider/status/{orderId}")
    public String riderStatus(@PathVariable String orderId, @RequestParam String status, HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"RIDER".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        orderService.updateStatus(orderId, status);
        return "redirect:/rider";
    }

    @PostMapping("/owner/order/status/{orderId}")
    public String ownerStatus(@PathVariable String orderId, @RequestParam String status,
                              HttpSession s,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"OWNER".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        Order order = orderService.byId(orderId);
        Restaurant restaurant = order == null ? null : restaurantService.byId(order.getRestaurantId());
        if (order != null && restaurant != null && u.getId().equals(restaurant.getOwnerId())) {
            // Forward-only flow. CANCELLED only allowed from PENDING.
            java.util.Map<String,Integer> rank = java.util.Map.of(
                    "PENDING", 0, "PREPARING", 1, "OUT_FOR_DELIVERY", 2, "DELIVERED", 3);
            String cur = order.getStatus() == null ? "PENDING" : order.getStatus().toUpperCase();
            String next = status == null ? "" : status.toUpperCase();
            boolean allowed;
            if ("CANCELLED".equals(next)) {
                allowed = "PENDING".equals(cur);
            } else if ("DELIVERED".equals(cur) || "CANCELLED".equals(cur)) {
                allowed = false;
            } else {
                Integer cr = rank.get(cur), nr = rank.get(next);
                allowed = cr != null && nr != null && nr >= cr;
            }
            if (allowed) {
                orderService.updateStatus(orderId, next);
            } else {
                ra.addFlashAttribute("statusError",
                        "Order " + orderId + ": cannot move from " + cur + " to " + next + ".");
            }
        }
        return "redirect:/owner";
    }
}
