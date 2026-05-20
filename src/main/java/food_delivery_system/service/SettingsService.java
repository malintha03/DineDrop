package food_delivery_system.service;

import food_delivery_system.model.Settings;
import food_delivery_system.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Central pricing service.
 * This service ensures consistent pricing logic across:
 * cart, checkout, order placement, payment, and reports.
 *
 * Service Layer: Contains business logic (not data storage).
 */
@Service
public class SettingsService {

    // Dependency Injection: connects service layer with repository layer
    @Autowired private SettingsRepository repo;

    //settings management

    // Retrieves current system settings from repository
    public Settings get() {
        return repo.load();
    }

    // Updates commission settings (admin-level operation)
    public void update(double restaurantCommissionPct, double riderCommissionPct) {

        // Validation: ensures no negative commission values
        if (restaurantCommissionPct < 0) restaurantCommissionPct = 0;
        if (riderCommissionPct < 0) riderCommissionPct = 0;

        // Creates new Settings object and saves it
        repo.save(new Settings(restaurantCommissionPct, riderCommissionPct));
    }

    // pricing business logic

    // Customer price = base price + restaurant commission
    public double customerPrice(double basePrice) {

        return round2(basePrice + commissionFromBase(basePrice));
    }

    // Calculates commission added on top of base food price
    public double commissionFromBase(double basePrice) {

        return round2(basePrice * (get().getRestaurantCommissionPct() / 100.0));
    }

    // Calculates website fee deducted from rider delivery fee
    public double riderWebsiteFee(double deliveryFee) {

        return round2(deliveryFee * (get().getRiderCommissionPct() / 100.0));
    }

    // Calculates final rider earnings after website deduction
    public double riderEarning(double deliveryFee) {

        return round2(deliveryFee - riderWebsiteFee(deliveryFee));
    }

    // utility method
    // Rounds values to 2 decimal places (currency formatting)
    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

}