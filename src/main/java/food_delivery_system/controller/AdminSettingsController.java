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
public class AdminSettingsController {

    @Autowired private SettingsService settingsService;
    @Autowired private RevenueService revenueService;
    @Autowired private AdminService adminService;

    private User requireAdmin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null || !"ADMIN".equalsIgnoreCase(u.getRole())) return null;
        return u;
    }

    @GetMapping("/admin/revenue")
    public String revenue(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        Settings st = settingsService.get();
        m.addAttribute("settings", st);
        m.addAttribute("today", revenueService.today());
        m.addAttribute("month", revenueService.thisMonth());
        m.addAttribute("overall", revenueService.overall());
        return "admin-revenue";
    }

    @GetMapping("/admin/settings")
    public String settings(HttpSession s, Model m) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        m.addAttribute("settings", settingsService.get());
        return "admin-settings";
    }

    @PostMapping("/admin/settings")
    public String saveSettings(@RequestParam double restaurantCommissionPct,
                               @RequestParam double riderCommissionPct,
                               HttpSession s, RedirectAttributes ra) {
        if (requireAdmin(s) == null) return "redirect:/admin-login";
        settingsService.update(restaurantCommissionPct, riderCommissionPct);
        ra.addFlashAttribute("success", "Commission settings updated");
        return "redirect:/admin/settings";
    }
}
