package food_delivery_system.service;

import food_delivery_system.model.User;
import food_delivery_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    @Autowired private UserRepository userRepo;

    public List<User> allUsers() { return userRepo.findAll(); }
    public List<User> byRole(String role) { return userRepo.findByRole(role); }
    public void deleteUser(String id) { userRepo.delete(id); }
    public User getUser(String id) { return userRepo.findById(id); }
    public void updateUser(User u) { userRepo.update(u); }
}
