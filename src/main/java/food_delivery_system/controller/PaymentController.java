package food_delivery_system.controller;

import food_delivery_system.model.User;
import food_delivery_system.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// @Controller marks this class as a Spring MVC Controller
// Controller layer handles HTTP requests in MVC architecture
@Controller
public class PaymentController {

    // Dependency Injection using @Autowired
    // Service layer object is automatically created by Spring
    @Autowired
    private PaymentService paymentService;

    // Handles GET request for "/payments" URL
    @GetMapping("/payments")
    public String myPayments(HttpSession session, Model model) {

        // HttpSession is used to store logged-in user data
        // Type casting Object to User model class
        User u = (User) session.getAttribute("user");

        // Simple validation / exception prevention
        // If user is not logged in, redirect to login page
        if (u == null) return "redirect:/login";

        // Calling service layer method to get customer payments
        // Service layer contains business logic
        // Encapsulation used because data is accessed through methods
        model.addAttribute("payments", paymentService.byCustomer(u.getId()));

        // Returns Thymeleaf/JSP view page
        return "view-payments";
    }
}