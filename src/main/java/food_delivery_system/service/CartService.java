package food_delivery_system.service;

import food_delivery_system.model.Cart;
import food_delivery_system.model.Food;
import food_delivery_system.repository.CartRepository;
import food_delivery_system.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    @Autowired private CartRepository cartRepo;
    @Autowired private FoodRepository foodRepo;

    public List<Cart> getCart(String customerId) { return cartRepo.findByCustomer(customerId); }

    public void addToCart(String customerId, String foodId, int qty) {
        Food f = foodRepo.findById(foodId);
        if (f == null) return;
        if (qty < 1) qty = 1;
        // If same food already in cart, increase qty
        for (Cart c : cartRepo.findByCustomer(customerId)) {
            if (c.getFoodId().equals(foodId)) {
                c.setQuantity(c.getQuantity() + qty);
                cartRepo.update(c);
                return;
            }
        }
        Cart c = new Cart(null, customerId, foodId, f.getName(), f.getRestaurantId(), f.getPrice(), qty);
        cartRepo.save(c);
    }

    public void updateQuantity(String cartId, int qty) {
        Cart c = cartRepo.findById(cartId);
        if (c == null) return;
        if (qty < 1) { cartRepo.delete(cartId); return; }
        c.setQuantity(qty);
        cartRepo.update(c);
    }
    public void remove(String cartId) { cartRepo.delete(cartId); }
    public void clear(String customerId) { cartRepo.clearForCustomer(customerId); }

    public double subtotal(String customerId) {
        return getCart(customerId).stream().mapToDouble(Cart::getSubtotal).sum();
    }
}
