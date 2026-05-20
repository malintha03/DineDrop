package food_delivery_system.controller;

import food_delivery_system.model.*;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@Controller
public class CartController {
    @Autowired private CartService cartService;
    @Autowired private FoodService foodService;
    @Autowired private RestaurantService restaurantService;
    @Autowired private OrderService orderService;
    @Autowired private ReviewService reviewService;
    @Autowired private SettingsService settingsService;
    @Autowired private CouponService couponService;

    private User requireCustomer(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"CUSTOMER".equalsIgnoreCase(u.getRole())) return null;
        return u;
    }

    @GetMapping("/customer")
    public String customerDashboard(HttpSession session, Model model) {
        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";
        model.addAttribute("restaurants", restaurantService.all());
        model.addAttribute("orders", orderService.byCustomer(u.getId()));
        model.addAttribute("reviews", reviewService.byCustomer(u.getId()));
        model.addAttribute("cartCount", cartService.getCart(u.getId()).size());
        return "customer-dashboard";
    }

    @GetMapping("/cart")
    public String viewCart(@RequestParam(required = false) String coupon,
                           HttpSession session, Model model) {
        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";
        List<Cart> items = cartService.getCart(u.getId());
        // Customer-facing pricing (base + website commission)
        double baseSub = 0, comm = 0;
        for (Cart c : items) {
            baseSub += c.getPrice() * c.getQuantity();
            comm += settingsService.commissionFromBase(c.getPrice()) * c.getQuantity();
        }
        double sub = baseSub + comm;
        double fee = items.isEmpty() ? 0 : 150.0;

        double discount = 0;
        String couponMsg = "";
        String appliedCoupon = "";
        if (coupon != null && !coupon.isBlank() && !items.isEmpty()) {
            CouponService.CouponResult r = couponService.apply(coupon,
                    items.get(0).getRestaurantId(), sub);
            couponMsg = r.message;
            if (r.ok) { discount = r.discount; appliedCoupon = r.code; }
        }

        model.addAttribute("items", items);
        model.addAttribute("foodCost", baseSub);
        model.addAttribute("commission", comm);
        model.addAttribute("subtotal", sub);
        model.addAttribute("deliveryFee", fee);
        model.addAttribute("discount", discount);
        model.addAttribute("total", sub + fee - discount);
        model.addAttribute("couponMessage", couponMsg);
        model.addAttribute("appliedCoupon", appliedCoupon);
        model.addAttribute("foodService", foodService);
        model.addAttribute("restaurantService", restaurantService);
        model.addAttribute("settingsService", settingsService);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String foodId,
                            @RequestParam(defaultValue = "1") int qty,
                            HttpSession session,
                            HttpServletRequest request,
                            RedirectAttributes ra) {
        User u = requireCustomer(session);
        if (u == null) return "redirect:/login";

        cartService.addToCart(u.getId(), foodId, qty);
        ra.addFlashAttribute("msg", "Added to cart");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/foods");
    }


    @PostMapping("/cart/update/{cartId}")
    public String updateQty(@PathVariable String cartId, @RequestParam int qty, HttpSession s) {
        if (requireCustomer(s) == null) return "redirect:/login";
        cartService.updateQuantity(cartId, qty);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{cartId}")
    public String remove(@PathVariable String cartId, HttpSession s) {
        if (requireCustomer(s) == null) return "redirect:/login";
        cartService.remove(cartId);
        return "redirect:/cart";
    }
}
