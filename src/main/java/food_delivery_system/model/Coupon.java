package food_delivery_system.model;

/**
 * Restaurant promo code. type = PERCENT | AMOUNT.
 * Discount applied to subtotal (food cost incl. commission).
 */
public class Coupon {
    private String id;
    private String restaurantId;
    private String code;
    private String type;          // PERCENT or AMOUNT
    private double value;         // percent (e.g. 10 = 10%) or flat amount in Rs.
    private double minOrder;      // minimum order subtotal required
    private String expiryDate;    // yyyy-MM-dd, blank = no expiry
    private boolean enabled;
    private String description;

    public Coupon() {}

    public Coupon(String id, String restaurantId, String code, String type, double value,
                  double minOrder, String expiryDate, boolean enabled, String description) {
        this.id = id; this.restaurantId = restaurantId;
        this.code = code == null ? "" : code.trim().toUpperCase();
        this.type = type == null ? "PERCENT" : type.toUpperCase();
        this.value = value;
        this.minOrder = minOrder;
        this.expiryDate = expiryDate == null ? "" : expiryDate;
        this.enabled = enabled;
        this.description = description == null ? "" : description;
    }

    public String getId(){return id;} public void setId(String v){this.id=v;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String v){this.restaurantId=v;}
    public String getCode(){return code;} public void setCode(String v){this.code = v == null ? "" : v.trim().toUpperCase();}
    public String getType(){return type;} public void setType(String v){this.type = v == null ? "PERCENT" : v.toUpperCase();}
    public double getValue(){return value;} public void setValue(double v){this.value=v;}
    public double getMinOrder(){return minOrder;} public void setMinOrder(double v){this.minOrder=v;}
    public String getExpiryDate(){return expiryDate;} public void setExpiryDate(String v){this.expiryDate = v == null ? "" : v;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){this.enabled=v;}
    public String getDescription(){return description;} public void setDescription(String v){this.description = v == null ? "" : v;}

    public boolean isExpired() {
        if (expiryDate == null || expiryDate.isBlank()) return false;
        try {
            return java.time.LocalDate.parse(expiryDate).isBefore(java.time.LocalDate.now());
        } catch (Exception e) { return false; }
    }

    /** Compute discount amount applied to a given subtotal. */
    public double computeDiscount(double subtotal) {
        if ("AMOUNT".equalsIgnoreCase(type)) return Math.min(value, subtotal);
        double d = subtotal * (value / 100.0);
        return Math.max(0, Math.min(d, subtotal));
    }
}
