package food_delivery_system.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** A placed order. status: PENDING | PREPARING | OUT_FOR_DELIVERY | DELIVERED | CANCELLED */
public class Order {
    private String id;
    private String customerId;
    private String restaurantId;
    private String items;        // "FoodName x2; FoodName x1"
    private double subtotal;     // customer-facing items subtotal (incl. commission, before discount)
    private double deliveryFee;
    private double total;        // final amount paid by customer
    private String address;
    private String city;
    private String status;
    private String riderId;
    private String createdAt;
    private String customerLatitude;
    private String customerLongitude;
    private String restaurantLatitude;
    private String restaurantLongitude;
    private String restaurantAddress;
    private String restaurantCity;

    // ===== Commission / promo bookkeeping (added v2) =====
    private double foodCost;            // raw price paid to restaurant (sum of base prices x qty)
    private double websiteCommission;   // website earnings from restaurant side
    private double riderWebsiteFee;     // website deduction from rider's delivery fee
    private double riderEarning;        // net delivery payout for rider
    private double discount;            // coupon discount applied
    private String couponCode;          // applied coupon code (blank if none)

    public Order() {}

    public Order(String id, String customerId, String restaurantId, String items,
                 double subtotal, double deliveryFee, double total, String address,
                 String city, String status, String riderId, String createdAt) {
        this(id, customerId, restaurantId, items, subtotal, deliveryFee, total, address,
                city, status, riderId, createdAt, "", "", "", "", "", "");
    }

    public Order(String id, String customerId, String restaurantId, String items,
                 double subtotal, double deliveryFee, double total, String address,
                 String city, String status, String riderId, String createdAt,
                 String customerLatitude, String customerLongitude,
                 String restaurantLatitude, String restaurantLongitude,
                 String restaurantAddress, String restaurantCity) {
        this(id, customerId, restaurantId, items, subtotal, deliveryFee, total, address,
                city, status, riderId, createdAt,
                customerLatitude, customerLongitude, restaurantLatitude, restaurantLongitude,
                restaurantAddress, restaurantCity,
                0, 0, 0, 0, 0, "");
    }

    public Order(String id, String customerId, String restaurantId, String items,
                 double subtotal, double deliveryFee, double total, String address,
                 String city, String status, String riderId, String createdAt,
                 String customerLatitude, String customerLongitude,
                 String restaurantLatitude, String restaurantLongitude,
                 String restaurantAddress, String restaurantCity,
                 double foodCost, double websiteCommission, double riderWebsiteFee,
                 double riderEarning, double discount, String couponCode) {
        this.id=id; this.customerId=customerId; this.restaurantId=restaurantId; this.items=items;
        this.subtotal=subtotal; this.deliveryFee=deliveryFee; this.total=total; this.address=address;
        this.city=city; this.status=status; this.riderId=riderId; this.createdAt=createdAt;
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

    public String getId(){return id;} public void setId(String i){this.id=i;}
    public String getCustomerId(){return customerId;} public void setCustomerId(String c){this.customerId=c;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String r){this.restaurantId=r;}
    public String getItems(){return items;} public void setItems(String i){this.items=i;}
    public double getSubtotal(){return subtotal;} public void setSubtotal(double s){this.subtotal=s;}
    public double getDeliveryFee(){return deliveryFee;} public void setDeliveryFee(double d){this.deliveryFee=d;}
    public double getTotal(){return total;} public void setTotal(double t){this.total=t;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
    public String getCity(){return city;} public void setCity(String c){this.city=c;}
    public String getStatus(){return status;} public void setStatus(String s){this.status=s;}
    public String getRiderId(){return riderId;} public void setRiderId(String r){this.riderId=r;}
    public String getCreatedAt(){return createdAt;} public void setCreatedAt(String c){this.createdAt=c;}
    public String getCustomerLatitude(){return customerLatitude;} public void setCustomerLatitude(String v){this.customerLatitude = v == null ? "" : v;}
    public String getCustomerLongitude(){return customerLongitude;} public void setCustomerLongitude(String v){this.customerLongitude = v == null ? "" : v;}
    public String getRestaurantLatitude(){return restaurantLatitude;} public void setRestaurantLatitude(String v){this.restaurantLatitude = v == null ? "" : v;}
    public String getRestaurantLongitude(){return restaurantLongitude;} public void setRestaurantLongitude(String v){this.restaurantLongitude = v == null ? "" : v;}
    public String getRestaurantAddress(){return restaurantAddress;} public void setRestaurantAddress(String v){this.restaurantAddress = v == null ? "" : v;}
    public String getRestaurantCity(){return restaurantCity;} public void setRestaurantCity(String v){this.restaurantCity = v == null ? "" : v;}

    public double getFoodCost(){return foodCost;} public void setFoodCost(double v){this.foodCost=v;}
    public double getWebsiteCommission(){return websiteCommission;} public void setWebsiteCommission(double v){this.websiteCommission=v;}
    public double getRiderWebsiteFee(){return riderWebsiteFee;} public void setRiderWebsiteFee(double v){this.riderWebsiteFee=v;}
    public double getRiderEarning(){return riderEarning;} public void setRiderEarning(double v){this.riderEarning=v;}
    public double getDiscount(){return discount;} public void setDiscount(double v){this.discount=v;}
    public String getCouponCode(){return couponCode;} public void setCouponCode(String v){this.couponCode = v == null ? "" : v;}

    public boolean hasCustomerCoordinates() {
        return customerLatitude != null && !customerLatitude.isBlank()
                && customerLongitude != null && !customerLongitude.isBlank();
    }

    public boolean hasRestaurantCoordinates() {
        return restaurantLatitude != null && !restaurantLatitude.isBlank()
                && restaurantLongitude != null && !restaurantLongitude.isBlank();
    }

    public boolean hasRestaurantLocation() {
        return hasRestaurantCoordinates()
                || (restaurantAddress != null && !restaurantAddress.isBlank())
                || (restaurantCity != null && !restaurantCity.isBlank());
    }

    public String getCustomerMapQuery() {
        if (hasCustomerCoordinates()) return customerLatitude + "," + customerLongitude;
        String q = ((address == null ? "" : address) + ", " + (city == null ? "" : city)).trim();
        return q.isBlank() || q.equals(",") ? "Sri Lanka" : q;
    }

    public String getRestaurantMapQuery() {
        if (hasRestaurantCoordinates()) return restaurantLatitude + "," + restaurantLongitude;
        String q = ((restaurantAddress == null ? "" : restaurantAddress) + ", " + (restaurantCity == null ? "" : restaurantCity)).trim();
        return q.isBlank() || q.equals(",") ? "Sri Lanka" : q;
    }

    public boolean isCompleted() { return "DELIVERED".equalsIgnoreCase(status); }
    public boolean isActive() {
        return !"DELIVERED".equalsIgnoreCase(status) && !"CANCELLED".equalsIgnoreCase(status);
    }

    private static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    public String getCustomerMapEmbedUrl() {
        return "https://maps.google.com/maps?q=" + enc(getCustomerMapQuery()) + "&output=embed";
    }

    public String getCustomerGoogleMapsUrl() {
        return "https://www.google.com/maps/search/?api=1&query=" + enc(getCustomerMapQuery());
    }

    public String getDirectionsUrl() {
        return "https://www.google.com/maps/dir/?api=1&origin=" + enc(getRestaurantMapQuery())
                + "&destination=" + enc(getCustomerMapQuery());
    }

    public String getRouteEmbedUrl() {
        return "https://maps.google.com/maps?saddr=" + enc(getRestaurantMapQuery())
                + "&daddr=" + enc(getCustomerMapQuery()) + "&output=embed";
    }
}
