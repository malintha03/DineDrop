package food_delivery_system.service;

import food_delivery_system.model.Review;
import food_delivery_system.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// @Service marks this class as Service layer
// Service layer contains business logic in MVC architecture
@Service
public class ReviewService {

    // Dependency Injection using @Autowired
    // Spring automatically injects ReviewRepository object
    @Autowired
    private ReviewRepository repo;

    // Add new review
    public Review add(Review r) {

        // Set current date and time for review creation
        // Java DateTime API used
        r.setCreatedAt(
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        // Save review using repository layer
        // Abstraction:
        // Service layer does not handle file/database details
        return repo.save(r);
    }

    // Return all reviews
    public List<Review> all() {

        return repo.findAll();
    }

    // Return reviews by customer ID
    public List<Review> byCustomer(String cid) {

        return repo.findByCustomer(cid);
    }

    // Return reviews by restaurant ID
    public List<Review> byRestaurant(String rid) {

        return repo.findByRestaurant(rid);
    }

    // Average rating for a restaurant
    public double averageForRestaurant(String rid) {
        java.util.List<Review> reviews = byRestaurant(rid);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    // Delete review using ID
    public void delete(String id) {

        repo.delete(id);
    }

    // Find review using ID
    public Review byId(String id) {

        return repo.findById(id);
    }

    // Update review details
    public Review update(Review r) {

        return repo.update(r);
    }

}