package food_delivery_system.repository;

import food_delivery_system.model.Payment;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

// @Repository marks this class as Repository layer in Spring
// Repository layer handles data storage and retrieval operations
@Repository
public class PaymentRepository {

    // File used to store payment records
    // File handling concept
    private static final String FILE = "payments.txt";

    // Dependency Injection using @Autowired
    // Spring automatically injects FileUtil object
    @Autowired
    private FileUtil fileUtil;

    // Returns all payment records
    public List<Payment> findAll() {

        // File handling:
        // Reading all lines from text file

        // Stream API and Collections used
        // Filtering empty lines and converting text into objects
        return fileUtil.readAllLines(FILE)
                .stream()
                .filter(l -> !l.isBlank())
                .map(this::parse)
                .collect(Collectors.toList());
    }

    // Save payment into file
    public Payment save(Payment p) {

        // Generate ID if ID is empty
        if (p.getId() == null || p.getId().isBlank())

            // Static method call
            p.setId("P-" + FileUtil.nextId());

        // Convert object into text line and append to file
        fileUtil.appendLine(FILE, toLine(p));

        return p;
    }

    // Find payments by customer ID
    public List<Payment> findByCustomer(String cid) {

        // Stream filtering operation
        return findAll()
                .stream()
                .filter(x -> cid.equals(x.getCustomerId()))
                .collect(Collectors.toList());
    }

    // Find payment using order ID
    public Payment findByOrder(String orderId) {

        // findFirst() returns first matching record
        return findAll()
                .stream()
                .filter(x -> orderId.equals(x.getOrderId()))
                .findFirst()
                .orElse(null);
    }

    // Delete payment record
    public void delete(String id) {

        // Create new list without deleted payment
        List<String> lines = findAll()
                .stream()
                .filter(x -> !x.getId().equals(id))
                .map(this::toLine)
                .collect(Collectors.toList());

        // Rewrite file with updated records
        fileUtil.writeAllLines(FILE, lines);
    }

    // Convert Payment object into file text format
    private String toLine(Payment p) {

        // Abstraction:
        // Complex joining logic hidden inside FileUtil
        return FileUtil.join(
                p.getId(),
                p.getOrderId(),
                p.getCustomerId(),
                p.getAmount(),
                p.getCardLast4(),
                p.getStatus(),
                p.getPaidAt()
        );
    }

    // Convert file line into Payment object
    private Payment parse(String l) {

        // Split line into array values
        String[] p = FileUtil.split(l);

        double amt = 0;

        try {

            // Convert String to double
            amt = Double.parseDouble(g(p, 3));

        } catch (Exception ignored) {

            // Exception handling:
            // Prevents program crash if conversion fails
        }

        // Create Payment object using parsed values
        return new Payment(
                g(p,0),
                g(p,1),
                g(p,2),
                amt,
                g(p,4),
                g(p,5),
                g(p,6)
        );
    }

    // Helper method for safe array access
    private static String g(String[] a, int i) {

        // Prevents ArrayIndexOutOfBoundsException
        return i < a.length ? a[i] : "";
    }
}