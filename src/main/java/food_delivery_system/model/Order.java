package food_delivery_system.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Represents a placed order in the food delivery system.
 *
 * Order status flow:
 * PENDING → PREPARING → OUT_FOR_DELIVERY → DELIVERED → CANCELLED
 */
public class Order {

    // ===================== BASIC ORDER INFO =====================

    private String id;               // Unique order ID
    private String customerId;       // Customer who placed the order
    private String restaurantId;     // Restaurant fulfilling the order

    // Human-readable item summary (e.g., "Burger x2; Coke x1")
    private String items;

    // Subtotal (customer-facing, includes commission before discount)
    private double subtotal;

    // Delivery charge applied to order
    private double deliveryFee;

    // Final amount paid by customer
    private double total;

    // Delivery details
    private String address;
    private String city;

    // Order status (PENDING, PREPARING, etc.)
    private String status;

    // Assigned rider ID (if any)
    private String riderId;

    // Order creation timestamp
    private String createdAt;

    // Order completion timestamp (saved when rider marks order as DELIVERED)
    private String completedAt;

    // ===================== LOCATION DATA =====================

    private String customerLatitude;
    private String customerLongitude;

    private String restaurantLatitude;
    private String restaurantLongitude;

    private String restaurantAddress;
    private String restaurantCity;

    // ===================== COMMISSION / FINANCIAL DATA =====================

    // Raw food cost (restaurant base earnings)
    private double foodCost;

    // Platform commission from restaurant orders
    private double websiteCommission;

    // Commission deducted from rider delivery earnings
    private double riderWebsiteFee;

    // Final rider earning after deductions
    private double riderEarning;

    // Discount applied via coupon
    private double discount;

    // Coupon code used
    private String couponCode;

    // ===================== CONSTRUCTORS =====================

    public Order() {}

    // Minimal constructor
    public Order(String id, String customerId, String restaurantId, String items,
                 double subtotal, double deliveryFee, double total, String address,
                 String city, String status, String riderId, String createdAt) {

        this(id, customerId, restaurantId, items, subtotal, deliveryFee, total,
                address, city, status, riderId, createdAt,
                "", "", "", "", "", "");
    }

    // Constructor with location data
    public Order(String id, String customerId, String restaurantId, String items,
                 double subtotal, double deliveryFee, double total, String address,
                 String city, String status, String riderId, String createdAt,
                 String customerLatitude, String customerLongitude,
                 String restaurantLatitude, String restaurantLongitude,
                 String restaurantAddress, String restaurantCity) {

        this(id, customerId, restaurantId, items, subtotal, deliveryFee, total,
                address, city, status, riderId, createdAt,
                customerLatitude, customerLongitude,
                restaurantLatitude, restaurantLongitude,
                restaurantAddress, restaurantCity,
                0, 0, 0, 0, 0, "");
    }

    // Full constructor (includes finance data)
    public Order(String id, String customerId, String restaurantId, String items,
                 double subtotal, double deliveryFee, double total, String address,
                 String city, String status, String riderId, String createdAt,
                 String customerLatitude, String customerLongitude,
                 String restaurantLatitude, String restaurantLongitude,
                 String restaurantAddress, String restaurantCity,
                 double foodCost, double websiteCommission,
                 double riderWebsiteFee, double riderEarning,
                 double discount, String couponCode) {

        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.total = total;
        this.address = address;
        this.city = city;
        this.status = status;
        this.riderId = riderId;
        this.createdAt = createdAt;
        this.completedAt = "";

        this.customerLatitude = customerLatitude == null ? "" : customerLatitude;
        this.customerLongitude = customerLongitude == null ? "" : customerLongitude;

        this.restaurantLatitude = restaurantLatitude == null ? "" : restaurantLatitude;
        this.restaurantLongitude = restaurantLongitude == null ? "" : restaurantLongitude;

        this.restaurantAddress = restaurantAddress == null ? "" : restaurantAddress;
        this.restaurantCity = restaurantCity == null ? "" : restaurantCity;

        this.foodCost = foodCost;
        this.websiteCommission = websiteCommission;
        this.riderWebsiteFee = riderWebsiteFee;
        this.riderEarning = riderEarning;
        this.discount = discount;
        this.couponCode = couponCode == null ? "" : couponCode;
    }

    // ===================== GETTERS & SETTERS =====================

    public String getId() { return id; }
    public void setId(String i) { this.id = i; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String c) { this.customerId = c; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String r) { this.restaurantId = r; }

    public String getItems() { return items; }
    public void setItems(String i) { this.items = i; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double s) { this.subtotal = s; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double d) { this.deliveryFee = d; }

    public double getTotal() { return total; }
    public void setTotal(double t) { this.total = t; }

    public String getAddress() { return address; }
    public void setAddress(String a) { this.address = a; }

    public String getCity() { return city; }
    public void setCity(String c) { this.city = c; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String r) { this.riderId = r; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String c) { this.createdAt = c; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt == null ? "" : completedAt; }

    public String getDisplayCompletedAt() {
        return completedAt == null || completedAt.isBlank() ? createdAt : completedAt;
    }

    // ===================== LOCATION GETTERS/SETTERS =====================

    public String getCustomerLatitude() { return customerLatitude; }
    public void setCustomerLatitude(String v) { this.customerLatitude = v == null ? "" : v; }

    public String getCustomerLongitude() { return customerLongitude; }
    public void setCustomerLongitude(String v) { this.customerLongitude = v == null ? "" : v; }

    public String getRestaurantLatitude() { return restaurantLatitude; }
    public void setRestaurantLatitude(String v) { this.restaurantLatitude = v == null ? "" : v; }

    public String getRestaurantLongitude() { return restaurantLongitude; }
    public void setRestaurantLongitude(String v) { this.restaurantLongitude = v == null ? "" : v; }

    public String getRestaurantAddress() { return restaurantAddress; }
    public void setRestaurantAddress(String v) { this.restaurantAddress = v == null ? "" : v; }

    public String getRestaurantCity() { return restaurantCity; }
    public void setRestaurantCity(String v) { this.restaurantCity = v == null ? "" : v; }

    // ===================== FINANCIAL GETTERS/SETTERS =====================

    public double getFoodCost() { return foodCost; }
    public void setFoodCost(double v) { this.foodCost = v; }

    public double getWebsiteCommission() { return websiteCommission; }
    public void setWebsiteCommission(double v) { this.websiteCommission = v; }

    public double getRiderWebsiteFee() { return riderWebsiteFee; }
    public void setRiderWebsiteFee(double v) { this.riderWebsiteFee = v; }

    public double getRiderEarning() { return riderEarning; }
    public void setRiderEarning(double v) { this.riderEarning = v; }

    public double getDiscount() { return discount; }
    public void setDiscount(double v) { this.discount = v; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String v) { this.couponCode = v == null ? "" : v; }

    // ===================== ORDER STATE HELPERS =====================

    // Check if customer location exists
    public boolean hasCustomerCoordinates() {
        return customerLatitude != null && !customerLatitude.isBlank()
                && customerLongitude != null && !customerLongitude.isBlank();
    }

    // Check if restaurant location exists
    public boolean hasRestaurantCoordinates() {
        return restaurantLatitude != null && !restaurantLatitude.isBlank()
                && restaurantLongitude != null && !restaurantLongitude.isBlank();
    }

    // Check if any restaurant location data exists
    public boolean hasRestaurantLocation() {
        return hasRestaurantCoordinates()
                || (restaurantAddress != null && !restaurantAddress.isBlank())
                || (restaurantCity != null && !restaurantCity.isBlank());
    }

    // ===================== MAP HELPERS =====================

    public String getCustomerMapQuery() {
        if (hasCustomerCoordinates())
            return customerLatitude + "," + customerLongitude;

        String q = ((address == null ? "" : address)
                + ", " + (city == null ? "" : city)).trim();

        return q.isBlank() || q.equals(",") ? "Sri Lanka" : q;
    }

    public String getRestaurantMapQuery() {
        if (hasRestaurantCoordinates())
            return restaurantLatitude + "," + restaurantLongitude;

        String q = ((restaurantAddress == null ? "" : restaurantAddress)
                + ", " + (restaurantCity == null ? "" : restaurantCity)).trim();

        return q.isBlank() || q.equals(",") ? "Sri Lanka" : q;
    }

    public boolean isCompleted() {
        return "DELIVERED".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return !"DELIVERED".equalsIgnoreCase(status)
                && !"CANCELLED".equalsIgnoreCase(status);
    }

    // URL encoder helper
    private static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    // Google Maps embed for customer location
    public String getCustomerMapEmbedUrl() {
        return "https://maps.google.com/maps?q="
                + enc(getCustomerMapQuery())
                + "&output=embed";
    }

    // Google Maps link for customer location
    public String getCustomerGoogleMapsUrl() {
        return "https://www.google.com/maps/search/?api=1&query="
                + enc(getCustomerMapQuery());
    }

    // Directions from restaurant → customer
    public String getDirectionsUrl() {
        return "https://www.google.com/maps/dir/?api=1&origin="
                + enc(getRestaurantMapQuery())
                + "&destination="
                + enc(getCustomerMapQuery());
    }

    // Embedded route map
    public String getRouteEmbedUrl() {
        return "https://maps.google.com/maps?saddr="
                + enc(getRestaurantMapQuery())
                + "&daddr="
                + enc(getCustomerMapQuery())
                + "&output=embed";
    }
}