package food_delivery_system.controller;

// Importing models and services
import food_delivery_system.model.*;
import food_delivery_system.service.*;

// Session handling
import jakarta.servlet.http.HttpSession;

// Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // Marks this class as a Spring MVC Controller
public class AdminSettingsController {

    // Injecting required services
    @Autowired private SettingsService settingsService;
    @Autowired private RevenueService revenueService;
    @Autowired private AdminService adminService;


    // Method to check whether logged user is ADMIN
    private User requireAdmin(HttpSession s) {

        // Get logged user from session
        User u = (User) s.getAttribute("user");

        // If user not logged in OR not admin
        if (u == null || !"ADMIN".equalsIgnoreCase(u.getRole()))
            return null;

        // Return admin user
        return u;
    }


    //Admin Revenue Page


    @GetMapping("/admin/revenue")
    public String revenue(HttpSession s, Model m) {

        // Only admin can access
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Get current commission settings
        Settings st = settingsService.get();

        // Send settings to frontend
        m.addAttribute("settings", st);

        // Revenue statistics
        m.addAttribute("today", revenueService.today());
        m.addAttribute("month", revenueService.thisMonth());
        m.addAttribute("overall", revenueService.overall());

        // Open admin-revenue.html
        return "admin-revenue";
    }


    //Admin Setting Page


    @GetMapping("/admin/settings")
    public String settings(HttpSession s, Model m) {

        // Check admin access
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Send settings object to frontend
        m.addAttribute("settings", settingsService.get());

        // Open admin-settings.html
        return "admin-settings";
    }

    // Save Commission Setting

    @PostMapping("/admin/settings")
    public String saveSettings(

            // Restaurant commission percentage
            @RequestParam double restaurantCommissionPct,

            // Rider commission percentage
            @RequestParam double riderCommissionPct,

            HttpSession s,
            RedirectAttributes ra) {

        // Only admin can update settings
        if (requireAdmin(s) == null)
            return "redirect:/admin-login";

        // Update commission settings
        settingsService.update(
                restaurantCommissionPct,
                riderCommissionPct
        );

        // Success message
        ra.addFlashAttribute(
                "success",
                "Commission settings updated"
        );

        // Redirect back to settings page
        return "redirect:/admin/settings";
    }
}