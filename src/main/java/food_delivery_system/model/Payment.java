package food_delivery_system.model;

/**
 * Dummy payment record model class.
 * Model class represents database/data objects in MVC architecture.
 */
public class Payment {

    // Encapsulation:
    // Private variables protect data from direct access

    private String id;
    private String orderId;
    private String customerId;
    private double amount;
    private String cardLast4;
    private String status; // PAID
    private String paidAt;

    // Default constructor
    // Used by frameworks like Spring/Jackson during object creation
    public Payment() {}

    // Parameterized constructor
    // Used to initialize object values quickly
    public Payment(String id,
                   String orderId,
                   String customerId,
                   double amount,
                   String cardLast4,
                   String status,
                   String paidAt) {

        // "this" keyword refers to current object variables
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.cardLast4 = cardLast4;
        this.status = status;
        this.paidAt = paidAt;
    }

    // Getter methods
    // Used to read private variable values

    public String getId() {
        return id;
    }

    // Setter methods
    // Used to modify private variable values

    public void setId(String i) {
        this.id = i;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String o) {
        this.orderId = o;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String c) {
        this.customerId = c;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double a) {
        this.amount = a;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String c) {
        this.cardLast4 = c;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        this.status = s;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String p) {
        this.paidAt = p;
    }
}