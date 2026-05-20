package food_delivery_system.model;

/** A line item in a customer's cart. */
public class Cart {
    private String id;
    private String customerId;
    private String foodId;
    private String foodName;
    private String restaurantId;
    private double price;
    private int quantity;

    public Cart() {}
    public Cart(String id, String customerId, String foodId, String foodName,
                String restaurantId, double price, int quantity) {
        this.id=id; this.customerId=customerId; this.foodId=foodId; this.foodName=foodName;
        this.restaurantId=restaurantId; this.price=price; this.quantity=quantity;
    }
    public String getId(){return id;} public void setId(String i){this.id=i;}
    public String getCustomerId(){return customerId;} public void setCustomerId(String c){this.customerId=c;}
    public String getFoodId(){return foodId;} public void setFoodId(String f){this.foodId=f;}
    public String getFoodName(){return foodName;} public void setFoodName(String f){this.foodName=f;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String r){this.restaurantId=r;}
    public double getPrice(){return price;} public void setPrice(double p){this.price=p;}
    public int getQuantity(){return quantity;} public void setQuantity(int q){this.quantity=q;}
    public double getSubtotal(){ return price * quantity; }
}
