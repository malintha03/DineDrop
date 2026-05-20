package food_delivery_system.service;

import food_delivery_system.model.*;
import food_delivery_system.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {
    @Autowired private OrderRepository repo;
    @Autowired private CartService cartService;
    @Autowired private SettingsService settingsService;

    /** Delivery fee = base 150 + 50 per km (city-based pseudo distance). */
    public double calculateDeliveryFee(String city, String restaurantCity) {
        if (city != null && restaurantCity != null && city.equalsIgnoreCase(restaurantCity)) return 150.0;
        return 350.0; // out of city
    }

    public Order place(String customerId, List<Cart> items, String address, String city,
                       String restaurantCity) {
        return place(customerId, items, address, city, restaurantCity,
                "", "", "", "", "", restaurantCity, 0.0, "");
    }

    public Order place(String customerId, List<Cart> items, String address, String city,
                       String restaurantCity, String customerLatitude, String customerLongitude,
                       String restaurantLatitude, String restaurantLongitude,
                       String restaurantAddress, String restaurantMapCity) {
        return place(customerId, items, address, city, restaurantCity,
                customerLatitude, customerLongitude, restaurantLatitude, restaurantLongitude,
                restaurantAddress, restaurantMapCity, 0.0, "");
    }

    /**
     * Place order with website commission breakdown and optional coupon.
     * Customer-facing item prices already include the website commission.
     */
    public Order place(String customerId, List<Cart> items, String address, String city,
                       String restaurantCity, String customerLatitude, String customerLongitude,
                       String restaurantLatitude, String restaurantLongitude,
                       String restaurantAddress, String restaurantMapCity,
                       double discount, String couponCode) {
        if (items == null || items.isEmpty()) return null;

        // Cart line prices = original restaurant base prices.
        // We treat the customer-facing subtotal as base + commission per-item.
        double foodCost = 0;       // restaurant earnings (base prices)
        double customerSubtotal = 0;
        double websiteCommission = 0;
        for (Cart c : items) {
            double base = c.getPrice() * c.getQuantity();
            double comm = settingsService.commissionFromBase(c.getPrice()) * c.getQuantity();
            foodCost += base;
            websiteCommission += comm;
            customerSubtotal += base + comm;
        }
        foodCost = SettingsService.round2(foodCost);
        websiteCommission = SettingsService.round2(websiteCommission);
        customerSubtotal = SettingsService.round2(customerSubtotal);

        if (discount < 0) discount = 0;
        if (discount > customerSubtotal) discount = customerSubtotal;
        discount = SettingsService.round2(discount);

        double fee = calculateDeliveryFee(city, restaurantCity);
        double riderFee = settingsService.riderWebsiteFee(fee);
        double riderEarn = settingsService.riderEarning(fee);
        double total = SettingsService.round2(customerSubtotal - discount + fee);

        StringBuilder sb = new StringBuilder();
        for (int i=0;i<items.size();i++) {
            if (i>0) sb.append("; ");
            Cart c = items.get(i);
            sb.append(c.getFoodName()).append(" x").append(c.getQuantity());
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Order o = new Order(null, customerId, items.get(0).getRestaurantId(), sb.toString(),
                customerSubtotal, fee, total, address, city, "PENDING", "", now,
                customerLatitude, customerLongitude, restaurantLatitude, restaurantLongitude,
                restaurantAddress, restaurantMapCity,
                foodCost, websiteCommission, riderFee, riderEarn, discount,
                couponCode == null ? "" : couponCode);
        return repo.save(o);
    }

    public List<Order> all() { return repo.findAll(); }
    public Order byId(String id) { return repo.findById(id); }
    public List<Order> byCustomer(String cid) { return repo.findByCustomer(cid); }
    public List<Order> byRestaurant(String rid) { return repo.findByRestaurant(rid); }
    public List<Order> byRider(String rid) { return repo.findByRider(rid); }
    public List<Order> unassigned() { return repo.findUnassigned(); }

    public List<Order> activeByCustomer(String cid)   { return byCustomer(cid).stream().filter(Order::isActive).toList(); }
    public List<Order> completedByCustomer(String cid){ return byCustomer(cid).stream().filter(Order::isCompleted).toList(); }
    public List<Order> activeByRestaurant(String rid)   { return byRestaurant(rid).stream().filter(Order::isActive).toList(); }
    public List<Order> completedByRestaurant(String rid){ return byRestaurant(rid).stream().filter(Order::isCompleted).toList(); }
    public List<Order> activeByRider(String rid)   { return byRider(rid).stream().filter(Order::isActive).toList(); }
    public List<Order> completedByRider(String rid){ return byRider(rid).stream().filter(Order::isCompleted).toList(); }

    public boolean updateStatus(String orderId, String status) {
        Order o = repo.findById(orderId);
        if (o == null) return false;
        applyStatus(o, status);
        repo.update(o);
        return true;
    }

    public boolean updateStatusByOwner(String orderId, String ownerId, java.util.Collection<String> ownerRestaurantIds, String status) {
        Order o = repo.findById(orderId);
        if (o == null || ownerId == null || ownerRestaurantIds == null || !ownerRestaurantIds.contains(o.getRestaurantId())) return false;
        String next = normalizeStatus(status);
        if ("OUT_FOR_DELIVERY".equals(next) || "DELIVERED".equals(next)) return false;
        if (!isOwnerStatusAllowed(o.getStatus(), next)) return false;
        applyStatus(o, next);
        repo.update(o);
        return true;
    }

    public boolean updateStatusByRider(String orderId, String riderId, String status) {
        Order o = repo.findById(orderId);
        if (o == null || riderId == null || !riderId.equals(o.getRiderId())) return false;
        String next = normalizeStatus(status);
        if (!("OUT_FOR_DELIVERY".equals(next) || "DELIVERED".equals(next))) return false;
        if (!isRiderStatusAllowed(o.getStatus(), next)) return false;
        applyStatus(o, next);
        repo.update(o);
        return true;
    }

    private void applyStatus(Order o, String status) {
        String next = normalizeStatus(status);
        o.setStatus(next);
        // Re-compute rider payout snapshot and save completion date/time when delivered.
        if ("DELIVERED".equalsIgnoreCase(next)) {
            o.setRiderWebsiteFee(settingsService.riderWebsiteFee(o.getDeliveryFee()));
            o.setRiderEarning(settingsService.riderEarning(o.getDeliveryFee()));
            if (o.getCompletedAt() == null || o.getCompletedAt().isBlank()) {
                o.setCompletedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
        }
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private boolean isOwnerStatusAllowed(String current, String next) {
        String cur = normalizeStatus(current);
        if (cur.isBlank()) cur = "PENDING";
        if ("CANCELLED".equals(next)) return "PENDING".equals(cur);
        if ("DELIVERED".equals(cur) || "CANCELLED".equals(cur)) return false;
        if (!"PENDING".equals(next) && !"PREPARING".equals(next)) return false;
        java.util.Map<String,Integer> rank = java.util.Map.of("PENDING", 0, "PREPARING", 1);
        Integer cr = rank.get(cur);
        Integer nr = rank.get(next);
        return cr != null && nr != null && nr >= cr;
    }

    private boolean isRiderStatusAllowed(String current, String next) {
        String cur = normalizeStatus(current);
        if ("DELIVERED".equals(cur) || "CANCELLED".equals(cur)) return false;
        java.util.Map<String,Integer> rank = java.util.Map.of("PREPARING", 1, "OUT_FOR_DELIVERY", 2, "DELIVERED", 3);
        Integer cr = rank.get(cur);
        Integer nr = rank.get(next);
        return cr != null && nr != null && nr >= cr;
    }

    public void assignRider(String orderId, String riderId) {
        Order o = repo.findById(orderId);
        if (o == null) return;
        o.setRiderId(riderId);
        if ("PENDING".equals(o.getStatus())) o.setStatus("PREPARING");
        repo.update(o);
    }

    public void delete(String id) { repo.delete(id); }

    /**
     * Customer-initiated cancellation. Allowed only while the order is still
     * PENDING and no rider has picked it up yet. Returns true if cancelled.
     */
    public boolean cancelByCustomer(String orderId, String customerId) {
        Order o = repo.findById(orderId);
        if (o == null) return false;
        if (!customerId.equals(o.getCustomerId())) return false;
        if (o.getRiderId() != null && !o.getRiderId().isBlank()) return false;
        if (!"PENDING".equalsIgnoreCase(o.getStatus())) return false;
        o.setStatus("CANCELLED");
        repo.update(o);
        return true;
    }

    private static double riderEarn(Order o, SettingsService s) {
        return o.getRiderEarning() > 0 ? o.getRiderEarning() : s.riderEarning(o.getDeliveryFee());
    }

    /** Today's payout for a rider (net, after website deduction). */
    public double riderPayoutToday(String riderId) {
        String today = LocalDate.now().toString();
        return byRider(riderId).stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .filter(o -> o.getDisplayCompletedAt() != null && o.getDisplayCompletedAt().startsWith(today))
                .mapToDouble(o -> riderEarn(o, settingsService))
                .sum();
    }
    public double riderPayoutMonth(String riderId) {
        String month = LocalDate.now().toString().substring(0, 7); // yyyy-MM
        return byRider(riderId).stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .filter(o -> o.getDisplayCompletedAt() != null && o.getDisplayCompletedAt().startsWith(month))
                .mapToDouble(o -> riderEarn(o, settingsService))
                .sum();
    }
    public double riderPayoutTotal(String riderId) {
        return byRider(riderId).stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(o -> o.getRiderEarning() > 0 ? o.getRiderEarning() : settingsService.riderEarning(o.getDeliveryFee()))
                .sum();
    }

    /** Owner income (restaurant earnings = foodCost) from DELIVERED orders. */
    public double ownerIncomeForRestaurants(java.util.Collection<String> restaurantIds, String dateOrMonthPrefix) {
        return all().stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .filter(o -> restaurantIds.contains(o.getRestaurantId()))
                .filter(o -> dateOrMonthPrefix == null || (o.getDisplayCompletedAt() != null && o.getDisplayCompletedAt().startsWith(dateOrMonthPrefix)))
                .mapToDouble(Order::getFoodCost)
                .sum();
    }
    public double ownerIncomeToday(java.util.Collection<String> restaurantIds) {
        return ownerIncomeForRestaurants(restaurantIds, LocalDate.now().toString());
    }
    public double ownerIncomeMonth(java.util.Collection<String> restaurantIds) {
        return ownerIncomeForRestaurants(restaurantIds, LocalDate.now().toString().substring(0,7));
    }
    public double ownerIncomeTotal(java.util.Collection<String> restaurantIds) {
        return ownerIncomeForRestaurants(restaurantIds, null);
    }

    /** Total website fee deducted from a rider (lifetime). */
    public double riderWebsiteFeeTotal(String riderId) {
        return byRider(riderId).stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(o -> o.getRiderWebsiteFee() > 0 ? o.getRiderWebsiteFee() : settingsService.riderWebsiteFee(o.getDeliveryFee()))
                .sum();
    }
}
