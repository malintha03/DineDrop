package food_delivery_system.model;

/**
 * Customer review for a food/restaurant after delivery.
 * Model class used to store review details.
 */
public class Review {

    // Encapsulation:
    // Private variables protect object data

    private String id;
    private String customerId;
    private String customerName;
    private String restaurantId;
    private String orderId;

    // Rating value between 1 to 5
    private int rating;

    private String comment;
    private String createdAt;

    // Default constructor
    // Required for frameworks and object creation
    public Review() {}

    // Parameterized constructor
    // Used to initialize object with values
    public Review(String id,
                  String customerId,
                  String customerName,
                  String restaurantId,
                  String orderId,
                  int rating,
                  String comment,
                  String createdAt) {

        // "this" keyword refers to current object variables
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.restaurantId = restaurantId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getter method for id
    // Used to access private variable value
    public String getId() {
        return id;
    }

    // Setter method for id
    // Used to modify private variable value
    public void setId(String i) {
        this.id = i;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String c) {
        this.customerId = c;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String c) {
        this.customerName = c;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String r) {
        this.restaurantId = r;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String o) {
        this.orderId = o;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int r) {
        this.rating = r;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String c) {
        this.comment = c;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String c) {
        this.createdAt = c;
    }
}