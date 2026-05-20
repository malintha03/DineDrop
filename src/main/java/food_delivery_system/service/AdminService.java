package food_delivery_system.service;

// Importing User model
import food_delivery_system.model.User;

// Importing User repository
import food_delivery_system.repository.UserRepository;

// Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Marks this class as a Service layer component
public class AdminService {

    // Injecting UserRepository
    @Autowired
    private UserRepository userRepo;



    // Returns all users from database(txt file)
    public List<User> allUsers() {

        return userRepo.findAll();
    }



    // Returns users filtered by role
    // Example: ADMIN, CUSTOMER, OWNER, RIDER
    public List<User> byRole(String role) {

        return userRepo.findByRole(role);
    }



    // Deletes a user using user ID
    public void deleteUser(String id) {

        userRepo.delete(id);
    }


    // Get single user


    // Finds one user by ID
    public User getUser(String id) {

        return userRepo.findById(id);
    }


    // Update user

    // Updates existing user details
    public void updateUser(User u) {

        userRepo.update(u);
    }
}