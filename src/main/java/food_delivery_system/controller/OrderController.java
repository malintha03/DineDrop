package food_delivery_system.controller;

// Importing models
import food_delivery_system.model.*;

// Importing services
import food_delivery_system.service.*;

// Repository for user lookup
import food_delivery_system.repository.UserRepository;

// Session handling
import jakarta.servlet.http.HttpSession;

// Spring MVC imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller // Marks this class as Spring MVC controller for order operations
public class OrderController {

    // Injecting required services
    @Autowired private OrderService orderService;
    @Autowired private CartService cartService;
    @Autowired private RestaurantService restaurantService;
    @Autowired private FoodService foodService;
    @Autowired private PaymentService paymentService;
    @Autowired private SettingsService settingsService;
    @Autowired private CouponService couponService;
    @Autowired private UserRepository userRepo;

    // ====================================================
    // HELPER: CHECK CUSTOMER LOGIN
    // ====================================================

    // Ensures session user is CUSTOMER
    private User requireCustomer(HttpSession session) {

        User u = (User) session.getAttribute("user");

        if (u == null || !"CUSTOMER".equalsIgnoreCase(u.getRole()))
            return null;

        return u;
    }

    // ====================================================
    // HELPER: CUSTOMER DISTRICT VALIDATION
    // ====================================================

    private boolean foodMatchesCustomerDistrict(User u, Food f) {
        if (u == null || u.getCity() == null || u.getCity().isBlank()) return true;
        if (f == null) return false;
        Restaurant r = restaurantService.byId(f.getRestaurantId());
        return r != null && u.getCity().trim().equalsIgnoreCase(r.getCity() == null ? "" : r.getCity().trim());
    }

    // ====================================================
    // HELPER: MAP DATA FOR CHECKOUT PAGE
    // ====================================================

    private void addCheckoutMapAttributes(Model model, Restaurant r) {

        // Restaurant location map data
        model.addAttribute("storeMapQuery",
                r != null ? r.getMapQuery() : "Sri Lanka");

        model.addAttribute("storeMapEmbedUrl",
                r != null ? r.getMapEmbedUrl()
                        : "https://maps.google.com/maps?q=Sri+Lanka&output=embed");

        model.addAttribute("storeMapsUrl",
                r != null ? r.getGoogleMapsUrl()
                        : "https://www.google.com/maps/search/?api=1&query=Sri+Lanka");
    }

    // ====================================================
    // CALCULATE CUSTOMER SUBTOTAL (FOOD + COMMISSION)
    // ====================================================

    private double customerSubtotal(List<Cart> items) {

        double s = 0;

        for (Cart c : items) {

            // base price + system commission
            s += (c.getPrice()
                    + settingsService.commissionFromBase(c.getPrice()))
                    * c.getQuantity();
        }

        return SettingsService.round2(s);
    }

    // ====================================================
    // APPLY COUPON + BREAKDOWN DATA
    // ====================================================

    private void addCheckoutBreakdown(Model model,
                                      List<Cart> items,
                                      String couponCode) {

        double subtotal = customerSubtotal(items);

        String restaurantId =
                items.isEmpty() ? "" : items.get(0).getRestaurantId();

        double discount = 0;
        String couponMsg = "";
        boolean couponOk = true;

        // Apply coupon if provided
        if (couponCode != null && !couponCode.isBlank()) {

            CouponService.CouponResult r =
                    couponService.apply(couponCode, restaurantId, subtotal);

            couponOk = r.ok;
            couponMsg = r.message;

            if (r.ok)
                discount = r.discount;

            model.addAttribute("appliedCoupon",
                    r.ok ? r.code : "");
        }

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", SettingsService.round2(discount));
        model.addAttribute("couponMessage", couponMsg);
        model.addAttribute("couponOk", couponOk);
    }

    // ====================================================
    // CHECKOUT PAGE (FROM CART)
    // ====================================================

    @GetMapping("/order/checkout")
    public String checkout(@RequestParam(required = false) String coupon,
                           HttpSession session,
                           Model model) {

        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";

        List<Cart> items = cartService.getCart(u.getId());
        if (items.isEmpty()) return "redirect:/cart";

        Restaurant r =
                restaurantService.byId(items.get(0).getRestaurantId());

        model.addAttribute("items", items);
        model.addAttribute("restaurant", r);
        model.addAttribute("buyNow", false);

        addCheckoutBreakdown(model, items, coupon);
        addCheckoutMapAttributes(model, r);

        return "payment";
    }

    // ====================================================
    // PLACE ORDER (FROM CART)
    // ====================================================

    @PostMapping("/order/place")
    public String place(@RequestParam String address,
                        @RequestParam String city,
                        @RequestParam(required=false) String customerLatitude,
                        @RequestParam(required=false) String customerLongitude,
                        @RequestParam String cardName,
                        @RequestParam String cardNumber,
                        @RequestParam String cardExpiry,
                        @RequestParam String cardCvv,
                        @RequestParam(required=false) String couponCode,
                        HttpSession session) {

        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";

        List<Cart> items = cartService.getCart(u.getId());
        if (items.isEmpty()) return "redirect:/cart";

        Restaurant r =
                restaurantService.byId(items.get(0).getRestaurantId());

        double subtotal = customerSubtotal(items);

        CouponService.CouponResult coupon =
                couponService.apply(couponCode,
                        items.get(0).getRestaurantId(),
                        subtotal);

        // Create order
        Order o = orderService.place(
                u.getId(),
                items,
                address,
                city,
                r != null ? r.getCity() : city,
                customerLatitude,
                customerLongitude,
                r != null ? r.getLatitude() : "",
                r != null ? r.getLongitude() : "",
                r != null ? r.getAddress() : "",
                r != null ? r.getCity() : "",
                coupon.ok ? coupon.discount : 0,
                coupon.ok ? coupon.code : ""
        );

        // Payment processing
        paymentService.pay(o.getId(), u.getId(),
                o.getTotal(), cardNumber);

        // Clear cart after order
        cartService.clear(u.getId());

        return "redirect:/customer/orders?placed=" + o.getId();
    }

    // ====================================================
    // BUY NOW FLOW (SKIPS CART)
    // ====================================================

    @PostMapping("/order/buy-now")
    public String buyNow(@RequestParam String foodId,
                         @RequestParam(defaultValue = "1") int qty,
                         HttpSession session,
                         Model model) {

        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";

        Food f = foodService.byId(foodId);
        if (f == null) return "redirect:/foods";
        if (!foodMatchesCustomerDistrict(u, f)) return "redirect:/foods";

        if (qty < 1) qty = 1;

        // Create temporary cart item
        Cart line = new Cart(null, u.getId(),
                f.getId(), f.getName(),
                f.getRestaurantId(),
                f.getPrice(),
                qty);

        List<Cart> items = new ArrayList<>();
        items.add(line);

        session.setAttribute("buyNowItems", items);

        Restaurant r =
                restaurantService.byId(f.getRestaurantId());

        model.addAttribute("items", items);
        model.addAttribute("restaurant", r);
        model.addAttribute("buyNow", true);

        addCheckoutBreakdown(model, items, null);
        addCheckoutMapAttributes(model, r);

        return "payment";
    }

    // ====================================================
    // PLACE ORDER (BUY NOW FLOW)
    // ====================================================

    @PostMapping("/order/buy-now/place")
    public String buyNowPlace(@RequestParam String address,
                              @RequestParam String city,
                              @RequestParam(required=false) String customerLatitude,
                              @RequestParam(required=false) String customerLongitude,
                              @RequestParam String cardName,
                              @RequestParam String cardNumber,
                              @RequestParam String cardExpiry,
                              @RequestParam String cardCvv,
                              @RequestParam(required=false) String couponCode,
                              HttpSession session) {

        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";

        List<Cart> items =
                (List<Cart>) session.getAttribute("buyNowItems");

        if (items == null || items.isEmpty())
            return "redirect:/foods";

        Restaurant r =
                restaurantService.byId(items.get(0).getRestaurantId());

        double subtotal = customerSubtotal(items);

        CouponService.CouponResult coupon =
                couponService.apply(couponCode,
                        items.get(0).getRestaurantId(),
                        subtotal);

        Order o = orderService.place(
                u.getId(),
                items,
                address,
                city,
                r != null ? r.getCity() : city,
                customerLatitude,
                customerLongitude,
                r != null ? r.getLatitude() : "",
                r != null ? r.getLongitude() : "",
                r != null ? r.getAddress() : "",
                r != null ? r.getCity() : "",
                coupon.ok ? coupon.discount : 0,
                coupon.ok ? coupon.code : ""
        );

        paymentService.pay(o.getId(),
                u.getId(),
                o.getTotal(),
                cardNumber);

        session.removeAttribute("buyNowItems");

        return "redirect:/customer/orders?placed=" + o.getId();
    }

    // ====================================================
    // ORDER HISTORY PAGE
    // ====================================================

    @GetMapping("/customer/orders")
    public String myOrders(HttpSession session,
                           Model model,
                           @RequestParam(required = false) String placed) {

        User u = (User) session.getAttribute("user");
        if (u == null) return "redirect:/login";

        // Get all customer orders
        List<Order> all = orderService.byCustomer(u.getId());

        // Show newest first
        java.util.Collections.reverse(all);

        model.addAttribute("orders", all);

        model.addAttribute("activeOrders",
                all.stream().filter(Order::isActive).toList());

        model.addAttribute("completedOrders",
                all.stream().filter(Order::isCompleted).toList());

        // Load rider info (phone, details etc.)
        java.util.Map<String, User> riders = new java.util.HashMap<>();

        for (Order o : all) {

            String rid = o.getRiderId();

            if (rid != null && !rid.isBlank()
                    && !riders.containsKey(rid)) {

                User r = userRepo.findById(rid);

                if (r != null)
                    riders.put(rid, r);
            }
        }

        model.addAttribute("riders", riders);

        model.addAttribute("placed", placed);
        model.addAttribute("restaurantService", restaurantService);

        return "view-orders";
    }

    // ====================================================
    // CUSTOMER CANCEL ORDER
    // ====================================================

    @PostMapping("/customer/orders/cancel/{id}")
    public String cancelOrder(@PathVariable String id,
                              HttpSession session,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";

        boolean ok =
                orderService.cancelByCustomer(id, u.getId());

        if (ok)
            ra.addFlashAttribute("cancelled", id);
        else
            ra.addFlashAttribute("cancelError",
                    "Order cannot be cancelled — it has already been picked or processed.");

        return "redirect:/customer/orders";
    }
}