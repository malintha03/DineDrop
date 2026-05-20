package food_delivery_system.model;

/**
 * Represents a single item in the customer's shopping cart.
 * Each Cart object stores details of one food item selected by a customer.
 */
public class Cart {

    // Unique identifier for this cart item
    private String id;

    // ID of the customer who owns this cart
    private String customerId;

    // Food item details
    private String foodId;
    private String foodName;

    // Restaurant that owns this food item
    private String restaurantId;

    // Price of a single unit of the food
    private double price;

    // Quantity selected by the customer
    private int quantity;

    // Default constructor (required for frameworks like Spring / serialization)
    public Cart() {}

    /**
     * Full constructor to create a cart item with all values.
     */
    public Cart(String id,
                String customerId,
                String foodId,
                String foodName,
                String restaurantId,
                double price,
                int quantity) {

        this.id = id;
        this.customerId = customerId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.restaurantId = restaurantId;
        this.price = price;
        this.quantity = quantity;
    }

    // ===================== GETTERS & SETTERS =====================

    public String getId() { return id; }
    public void setId(String i) { this.id = i; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String c) { this.customerId = c; }

    public String getFoodId() { return foodId; }
    public void setFoodId(String f) { this.foodId = f; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String f) { this.foodName = f; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String r) { this.restaurantId = r; }

    public double getPrice() { return price; }
    public void setPrice(double p) { this.price = p; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }

    /**
     * Calculates subtotal for this cart item (price × quantity).
     */
    public double getSubtotal() {
        return price * quantity;
    }
}