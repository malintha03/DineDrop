package food_delivery_system.model;

/**
 * Platform settings — global website commission configuration.
 * This is a Model class in MVC architecture.
 * It represents system-wide configuration data.
 *
 * Single-row entity persisted in settings.txt.
 *
 * restaurantCommissionPct = % added on top of restaurant food price (paid by customer).
 * riderCommissionPct      = % deducted from delivery fee (paid by rider).
 */
public class Settings {

    // Encapsulation: private variables protect global configuration data
    private double restaurantCommissionPct;
    private double riderCommissionPct;

    // Default constructor
    // Used when system loads default configuration values
    public Settings() {
        // Default commission values (business logic defaults)
        this.restaurantCommissionPct = 5.0;
        this.riderCommissionPct = 10.0;
    }

    // Parameterized constructor
    // Used when loading settings from file or admin panel
    public Settings(double restaurantCommissionPct, double riderCommissionPct) {
        this.restaurantCommissionPct = restaurantCommissionPct;
        this.riderCommissionPct = riderCommissionPct;
    }

    // Getter method for restaurant commission percentage
    public double getRestaurantCommissionPct() {
        return restaurantCommissionPct;
    }

    // Setter method for restaurant commission percentage
    public void setRestaurantCommissionPct(double v) {
        this.restaurantCommissionPct = v;
    }

    // Getter method for rider commission percentage
    public double getRiderCommissionPct() {
        return riderCommissionPct;
    }

    // Setter method for rider commission percentage
    public void setRiderCommissionPct(double v) {
        this.riderCommissionPct = v;
    }

}