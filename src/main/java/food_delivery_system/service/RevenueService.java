package food_delivery_system.service;

import food_delivery_system.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Aggregates website revenue from completed orders. */
// Service layer: handles business logic for revenue calculations
@Service
public class RevenueService {

    // Dependency Injection: OrderService provides order data
    @Autowired private OrderService orderService;

    // INNER CLASS (DTO / DATA TRANSFER OBJECT
    // This class represents calculated revenue statistics
    public static class Stats {

        // Total commission earned from restaurants
        public double restaurantCommission;

        // Total commission earned from riders
        public double riderCommission;

        // Total revenue (restaurant + rider commission)
        public double total;

        // Number of completed orders considered in calculation
        public int completedOrders;

        // Constructor initializes computed statistics
        public Stats(double r, double rd, int n) {

            // Utility method used for rounding values (external service)
            this.restaurantCommission = SettingsService.round2(r);
            this.riderCommission = SettingsService.round2(rd);

            // Total revenue calculation
            this.total = SettingsService.round2(r + rd);

            this.completedOrders = n;
        }
    }

    // helper method

    // Checks whether an order is completed (business rule)
    private boolean isCompleted(Order o) {

        // Only DELIVERED orders are considered revenue-generating
        return "DELIVERED".equalsIgnoreCase(o.getStatus());
    }

    // Revenue calculations

    // Calculates overall revenue from all completed orders
    public Stats overall() {

        return computeFor(
                orderService.all().stream()
                        .filter(this::isCompleted)
                        .toList()
        );
    }

    // Calculates today's revenue
    public Stats today() {

        // Gets current date in yyyy-MM-dd format
        String today = LocalDate.now().toString();

        return computeFor(
                orderService.all().stream()

                        // Only completed orders
                        .filter(this::isCompleted)

                        // Filters orders created today
                        .filter(o -> o.getCreatedAt() != null
                                && o.getCreatedAt().startsWith(today))

                        .toList()
        );
    }

    // Calculates current month's revenue
    public Stats thisMonth() {

        // Format: yyyy-MM
        String month = YearMonth.now().toString();

        return computeFor(
                orderService.all().stream()

                        // Only delivered orders
                        .filter(this::isCompleted)

                        // Filters orders belonging to current month
                        .filter(o -> o.getCreatedAt() != null
                                && o.getCreatedAt().startsWith(month))

                        .toList()
        );
    }

    //core calculation method

    // Computes revenue statistics for a given list of orders
    private Stats computeFor(List<Order> orders) {

        // Sum of website commission from all orders
        double r = orders.stream()
                .mapToDouble(Order::getWebsiteCommission)
                .sum();

        // Sum of rider commission from all orders
        double rd = orders.stream()
                .mapToDouble(Order::getRiderWebsiteFee)
                .sum();

        // Return calculated statistics object
        return new Stats(r, rd, orders.size());
    }

}