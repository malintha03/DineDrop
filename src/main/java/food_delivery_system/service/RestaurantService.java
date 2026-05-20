package food_delivery_system.service;

import food_delivery_system.model.Restaurant;
import food_delivery_system.repository.FoodRepository;
import food_delivery_system.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {
    @Autowired private RestaurantRepository repo;
    @Autowired private FoodRepository foodRepo;

    public List<Restaurant> all() { return repo.findAll(); }
    public List<Restaurant> byOwner(String ownerId) { return repo.findByOwner(ownerId); }
    public boolean ownerHasRestaurant(String ownerId) { return !byOwner(ownerId).isEmpty(); }
    public Restaurant byId(String id) { return repo.findById(id); }
    public Restaurant add(Restaurant r) { return repo.save(r); }
    public void update(Restaurant r) { repo.update(r); }
    public void delete(String id) { foodRepo.deleteByRestaurant(id); repo.delete(id); }
}
