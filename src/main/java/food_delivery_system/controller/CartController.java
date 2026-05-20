package food_delivery_system.controller;

// Importing models
import food_delivery_system.model.*;

// Importing services
import food_delivery_system.service.*;

// Session handling
import jakarta.servlet.http.HttpSession;

// Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Request handling
import jakarta.servlet.http.HttpServletRequest;

// Flash messages
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller // Marks this class as Spring MVC Controller
public class CartController {

    // Injecting required services
    @Autowired private CartService cartService;
    @Autowired private FoodService foodService;
    @Autowired private RestaurantService restaurantService;
    @Autowired private OrderService orderService;
    @Autowired private ReviewService reviewService;
    @Autowired private SettingsService settingsService;
    @Autowired private CouponService couponService;

    // ====================================================
    // CHECK CUSTOMER LOGIN
    // ====================================================

    // Ensures logged user is CUSTOMER
    private User requireCustomer(HttpSession s) {

        // Get user from session
        User u = (User) s.getAttribute("user");

        // If no user OR not CUSTOMER
        if (u == null || !"CUSTOMER".equalsIgnoreCase(u.getRole()))
            return null;

        // Return valid customer
        return u;
    }

    // ====================================================
    // CUSTOMER DISTRICT VALIDATION
    // ====================================================

    private boolean foodMatchesCustomerDistrict(User u, String foodId) {
        if (u == null || u.getCity() == null || u.getCity().isBlank()) return true;
        Food f = foodService.byId(foodId);
        if (f == null) return false;
        Restaurant r = restaurantService.byId(f.getRestaurantId());
        return r != null && u.getCity().trim().equalsIgnoreCase(r.getCity() == null ? "" : r.getCity().trim());
    }

    // ====================================================
    // CUSTOMER DASHBOARD
    // ====================================================

    @GetMapping("/customer")
    public String customerDashboard(HttpSession session, Model model) {

        // Validate customer
        User u = requireCustomer(session);

        if (u == null)
            return "redirect:/login";

        // Send city-matched restaurant list
        java.util.List<Restaurant> visibleRestaurants = restaurantService.all();
        if (u.getCity() != null && !u.getCity().isBlank()) {
            String city = u.getCity().trim();
            visibleRestaurants = visibleRestaurants.stream()
                    .filter(r -> city.equalsIgnoreCase(r.getCity() == null ? "" : r.getCity().trim()))
                    .toList();
        }
        model.addAttribute("restaurants", visibleRestaurants);

        // Send customer orders
        model.addAttribute("orders",
                orderService.byCustomer(u.getId()));

        // Send customer reviews
        model.addAttribute("reviews",
                reviewService.byCustomer(u.getId()));

        // Send cart item count
        model.addAttribute("cartCount",
                cartService.getCart(u.getId()).size());

        // Open customer-dashboard.html
        return "customer-dashboard";
    }

    // ====================================================
    // VIEW CART
    // ====================================================

    @GetMapping("/cart")
    public String viewCart(

            // Optional coupon code
            @RequestParam(required = false) String coupon,

            HttpSession session,
            Model model) {

        // Validate customer
        User u = requireCustomer(session);

        if (u == null)
            return "redirect:/login";

        // Get cart items
        List<Cart> items = cartService.getCart(u.getId());

        /*
         Customer pricing calculation:
         Base food price + website commission
        */

        double baseSub = 0; // Total food price
        double comm = 0;    // Total commission

        // Calculate subtotal
        for (Cart c : items) {

            // Food total
            baseSub += c.getPrice() * c.getQuantity();

            // Commission total
            comm += settingsService
                    .commissionFromBase(c.getPrice())
                    * c.getQuantity();
        }

        // Final subtotal
        double sub = baseSub + comm;

        // Delivery fee
        double fee = items.isEmpty() ? 0 : 150.0;

        // Coupon discount values
        double discount = 0;
        String couponMsg = "";
        String appliedCoupon = "";

        // Apply coupon if available
        if (coupon != null
                && !coupon.isBlank()
                && !items.isEmpty()) {

            // Validate and apply coupon
            CouponService.CouponResult r =
                    couponService.apply(
                            coupon,
                            items.get(0).getRestaurantId(),
                            sub
                    );

            // Coupon response message
            couponMsg = r.message;

            // If valid coupon
            if (r.ok) {

                discount = r.discount;
                appliedCoupon = r.code;
            }
        }

        // ====================================================
        // SEND DATA TO FRONTEND
        // ====================================================

        model.addAttribute("items", items);

        // Food total only
        model.addAttribute("foodCost", baseSub);

        // Website commission
        model.addAttribute("commission", comm);

        // Subtotal
        model.addAttribute("subtotal", sub);

        // Delivery charge
        model.addAttribute("deliveryFee", fee);

        // Coupon discount
        model.addAttribute("discount", discount);

        // Final total
        model.addAttribute("total",
                sub + fee - discount);

        // Coupon messages
        model.addAttribute("couponMessage", couponMsg);
        model.addAttribute("appliedCoupon", appliedCoupon);

        // Send services for frontend usage
        model.addAttribute("foodService", foodService);
        model.addAttribute("restaurantService", restaurantService);
        model.addAttribute("settingsService", settingsService);

        // Open cart.html
        return "cart";
    }

    // ====================================================
    // ADD ITEM TO CART
    // ====================================================

    @PostMapping("/cart/add")
    public String addToCart(

            // Food ID
            @RequestParam String foodId,

            // Quantity (default = 1)
            @RequestParam(defaultValue = "1") int qty,

            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes ra) {

        // Validate customer
        User u = requireCustomer(session);

        if (u == null)
            return "redirect:/login";

        if (!foodMatchesCustomerDistrict(u, foodId)) {
            ra.addFlashAttribute("msg", "This food is not available in your selected district.");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/foods");
        }

        // Add item to cart
        cartService.addToCart(
                u.getId(),
                foodId,
                qty
        );

        // Success message
        ra.addFlashAttribute("msg", "Added to cart");

        // Redirect back to previous page
        String referer = request.getHeader("Referer");

        return "redirect:" +
                (referer != null ? referer : "/foods");
    }

    // ====================================================
    // UPDATE CART ITEM QUANTITY
    // ====================================================

    @PostMapping("/cart/update/{cartId}")
    public String updateQty(

            // Cart item ID
            @PathVariable String cartId,

            // New quantity
            @RequestParam int qty,

            HttpSession s) {

        // Validate customer
        if (requireCustomer(s) == null)
            return "redirect:/login";

        // Update quantity
        cartService.updateQuantity(cartId, qty);

        return "redirect:/cart";
    }

    // ====================================================
    // REMOVE ITEM FROM CART
    // ====================================================

    @PostMapping("/cart/remove/{cartId}")
    public String remove(

            // Cart item ID
            @PathVariable String cartId,

            HttpSession s) {

        // Validate customer
        if (requireCustomer(s) == null)
            return "redirect:/login";

        // Remove item
        cartService.remove(cartId);

        return "redirect:/cart";
    }
}