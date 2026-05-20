package food_delivery_system.controller;

import food_delivery_system.model.User;
import food_delivery_system.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private static final java.util.List<String> SRI_LANKAN_DISTRICTS = java.util.List.of(
            "Ampara", "Anuradhapura", "Badulla", "Batticaloa", "Colombo",
            "Galle", "Gampaha", "Hambantota", "Jaffna", "Kalutara",
            "Kandy", "Kegalle", "Kilinochchi", "Kurunegala", "Mannar",
            "Matale", "Matara", "Monaragala", "Mullaitivu", "Nuwara Eliya",
            "Polonnaruwa", "Puttalam", "Ratnapura", "Trincomalee", "Vavuniya"
    );

    @ModelAttribute("districts")
    public java.util.List<String> districts() { return SRI_LANKAN_DISTRICTS; }

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Autowired private CartService cartService;
    @Autowired private food_delivery_system.service.SettingsService settingsService;

    @ModelAttribute("settingsService")
    public food_delivery_system.service.SettingsService settingsService() { return settingsService; }

    @ModelAttribute("googleMapsApiKey")
    public String googleMapsApiKey() {
        return googleMapsApiKey;
    }

    @ModelAttribute("globalCartCount")
    public int cartCount(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User)) return 0;
        User u = (User) userObj;
        if (!"CUSTOMER".equalsIgnoreCase(u.getRole())) return 0;
        try {
            return cartService.getCart(u.getId()).stream()
                    .mapToInt(c -> Math.max(1, c.getQuantity())).sum();
        } catch (Exception e) {
            return 0;
        }
    }
}
