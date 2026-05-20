package food_delivery_system.controller;

import food_delivery_system.model.*;
import food_delivery_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CouponController {

    @Autowired private CouponService couponService;
    @Autowired private RestaurantService restaurantService;

    private User requireOwner(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"OWNER".equalsIgnoreCase(u.getRole())) return null;
        return u;
    }

    @GetMapping("/owner/coupons")
    public String list(HttpSession s, Model m) {
        User u = requireOwner(s);
        if (u == null) return "redirect:/login";
        List<Restaurant> myRestaurants = restaurantService.byOwner(u.getId());
        // Collect coupons across all the owner's restaurants
        java.util.List<Coupon> all = new java.util.ArrayList<>();
        for (Restaurant r : myRestaurants) all.addAll(couponService.byRestaurant(r.getId()));
        m.addAttribute("coupons", all);
        m.addAttribute("restaurants", myRestaurants);
        m.addAttribute("restaurantService", restaurantService);
        return "manage-coupons";
    }

    @PostMapping("/owner/coupons/save")
    public String save(@RequestParam(required = false) String id,
                       @RequestParam String restaurantId,
                       @RequestParam String code,
                       @RequestParam(defaultValue = "PERCENT") String type,
                       @RequestParam double value,
                       @RequestParam(defaultValue = "0") double minOrder,
                       @RequestParam(required = false) String expiryDate,
                       @RequestParam(required = false) String enabled,
                       @RequestParam(required = false) String description,
                       HttpSession s, RedirectAttributes ra) {
        boolean enabledFlag = "true".equalsIgnoreCase(enabled) || "on".equalsIgnoreCase(enabled) || "1".equals(enabled);
        User u = requireOwner(s);
        if (u == null) return "redirect:/login";
        Restaurant r = restaurantService.byId(restaurantId);
        if (r == null || !u.getId().equals(r.getOwnerId())) {
            ra.addFlashAttribute("error", "Invalid restaurant");
            return "redirect:/owner/coupons";
        }
        Coupon c = (id != null && !id.isBlank()) ? couponService.byId(id) : null;
        if (c == null) {
            c = new Coupon(null, restaurantId, code, type, value, minOrder, expiryDate, enabledFlag, description);
            couponService.save(c);
            ra.addFlashAttribute("success", "Coupon created");
        } else {
            c.setRestaurantId(restaurantId);
            c.setCode(code); c.setType(type); c.setValue(value); c.setMinOrder(minOrder);
            c.setExpiryDate(expiryDate); c.setEnabled(enabledFlag); c.setDescription(description);
            couponService.update(c);
            ra.addFlashAttribute("success", "Coupon updated");
        }
        // If posted from owner-dashboard inline form, send the user back there.
        String referer = ((jakarta.servlet.http.HttpServletRequest)
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest())
                .getHeader("Referer");
        if (referer != null && referer.contains("/owner") && !referer.contains("/owner/coupons")) {
            return "redirect:/owner";
        }
        return "redirect:/owner/coupons";
    }

    @PostMapping("/owner/coupons/toggle/{id}")
    public String toggle(@PathVariable String id, HttpSession s) {
        if (requireOwner(s) == null) return "redirect:/login";
        Coupon c = couponService.byId(id);
        if (c != null) { c.setEnabled(!c.isEnabled()); couponService.update(c); }
        return "redirect:/owner/coupons";
    }

    @PostMapping("/owner/coupons/delete/{id}")
    public String delete(@PathVariable String id, HttpSession s) {
        if (requireOwner(s) == null) return "redirect:/login";
        couponService.delete(id);
        return "redirect:/owner/coupons";
    }
}
