package food_delivery_system.service;

import food_delivery_system.model.Food;
import food_delivery_system.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service annotation marks this class as Service Layer in MVC architecture
// Service layer contains business logic and acts as a bridge between Controller and Repository
@Service
public class FoodService {

    // Dependency Injection using Spring (@Autowired)
    // Service depends on Repository for data access
    @Autowired private FoodRepository repo;

    // business method

    // Returns all food items
    public List<Food> all() {
        return repo.findAll();
    }

    // Returns food item by ID
    public Food byId(String id) {
        return repo.findById(id);
    }

    // Returns foods by restaurant ID
    public List<Food> byRestaurant(String rid) {
        return repo.findByRestaurant(rid);
    }

    // Search food items by keyword
    public List<Food> search(String q) {
        return repo.search(q);
    }

    // Add new food item
    public Food add(Food f) {
        return repo.save(f);
    }

    // Update existing food item
    public void update(Food f) {
        repo.update(f);
    }

    // Delete food item by ID
    public void delete(String id) {
        repo.delete(id);
    }
}