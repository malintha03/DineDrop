package food_delivery_system.service;

// Importing models
import food_delivery_system.model.Cart;
import food_delivery_system.model.Food;

// Importing repositories
import food_delivery_system.repository.CartRepository;
import food_delivery_system.repository.FoodRepository;

// Spring Service annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for all Cart-related business logic.
 * Handles adding items, updating quantities, removing items, and calculating subtotal.
 */
@Service
public class CartService {

    // Injecting Cart and Food repositories
    @Autowired private CartRepository cartRepo;
    @Autowired private FoodRepository foodRepo;

    // ====================================================
    // GET ALL CART ITEMS FOR A CUSTOMER
    // ====================================================

    public List<Cart> getCart(String customerId) {

        // Fetch cart items from repository by customer ID
        return cartRepo.findByCustomer(customerId);
    }

    // ====================================================
    // ADD ITEM TO CART
    // ====================================================

    public void addToCart(String customerId, String foodId, int qty) {

        // Find food item from database
        Food f = foodRepo.findById(foodId);

        // If food does not exist, stop process
        if (f == null) return;

        // Ensure minimum quantity is 1
        if (qty < 1) qty = 1;

        // Check if same food already exists in cart
        for (Cart c : cartRepo.findByCustomer(customerId)) {

            if (c.getFoodId().equals(foodId)) {

                // Increase quantity instead of adding duplicate row
                c.setQuantity(c.getQuantity() + qty);
                cartRepo.update(c);
                return;
            }
        }

        // If not found in cart, create new cart item
        Cart c = new Cart(
                null,
                customerId,
                foodId,
                f.getName(),
                f.getRestaurantId(),
                f.getPrice(),
                qty
        );

        // Save new cart item
        cartRepo.save(c);
    }

    // ====================================================
    // UPDATE QUANTITY OF CART ITEM
    // ====================================================

    public void updateQuantity(String cartId, int qty) {

        // Find cart item
        Cart c = cartRepo.findById(cartId);

        if (c == null) return;

        // If quantity is invalid, remove item
        if (qty < 1) {
            cartRepo.delete(cartId);
            return;
        }

        // Update quantity
        c.setQuantity(qty);
        cartRepo.update(c);
    }

    // ====================================================
    // REMOVE ITEM FROM CART
    // ====================================================

    public void remove(String cartId) {

        cartRepo.delete(cartId);
    }

    // ====================================================
    // CLEAR ENTIRE CART
    // ====================================================

    public void clear(String customerId) {

        cartRepo.clearForCustomer(customerId);
    }

    // ====================================================
    // CALCULATE CART SUBTOTAL
    // ====================================================

    public double subtotal(String customerId) {

        // Sum of (price × quantity) for all items
        return getCart(customerId)
                .stream()
                .mapToDouble(Cart::getSubtotal)
                .sum();
    }
}