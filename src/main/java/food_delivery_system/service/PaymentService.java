package food_delivery_system.service;

import food_delivery_system.model.Payment;
import food_delivery_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// @Service marks this class as Service layer
// Service layer contains business logic in MVC architecture
@Service
public class PaymentService {

    // Dependency Injection using @Autowired
    // Spring automatically injects PaymentRepository object
    @Autowired
    private PaymentRepository repo;

    // Method used to process payment
    public Payment pay(String orderId,
                       String customerId,
                       double amount,
                       String cardNumber) {

        // Ternary operator used for validation
        // If card number is invalid, use default value "0000"

        // replaceAll("\\s","") removes spaces from card number
        // substring() gets last 4 digits of card
        String last4 = cardNumber == null || cardNumber.length() < 4
                ? "0000"
                : cardNumber.replaceAll("\\s","")
                .substring(
                        Math.max(
                                0,
                                cardNumber.replaceAll("\\s","").length() - 4
                        )
                );

        // Get current date and time
        // Java DateTime API used
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Create Payment object and save using repository layer
        // Abstraction:
        // Service does not know file/database details
        return repo.save(
                new Payment(
                        null,
                        orderId,
                        customerId,
                        amount,
                        last4,
                        "PAID",
                        now
                )
        );
    }

    // Return all payments
    public List<Payment> all() {

        // Delegating operation to repository layer
        return repo.findAll();
    }

    // Find payments by customer ID
    public List<Payment> byCustomer(String cid) {

        return repo.findByCustomer(cid);
    }

    // Find payment using order ID
    public Payment byOrder(String orderId) {

        return repo.findByOrder(orderId);
    }

    // Delete payment record
    public void delete(String id) {

        repo.delete(id);
    }
}