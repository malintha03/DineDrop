package food_delivery_system.service;

import food_delivery_system.model.Coupon;
import food_delivery_system.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {

    @Autowired private CouponRepository repo;

    public List<Coupon> all() { return repo.findAll(); }
    public List<Coupon> byRestaurant(String rid) { return repo.findByRestaurant(rid); }
    public Coupon byId(String id) { return repo.findById(id); }
    public Coupon byCode(String code, String restaurantId) {
        return repo.findByCodeAndRestaurant(code, restaurantId);
    }
    public Coupon save(Coupon c) { return repo.save(c); }
    public void update(Coupon c) { repo.update(c); }
    public void delete(String id) { repo.delete(id); }

    /** Result holder for coupon application. */
    public static class CouponResult {
        public final boolean ok;
        public final double discount;
        public final String code;
        public final String message;
        public CouponResult(boolean ok, double discount, String code, String message) {
            this.ok = ok; this.discount = discount;
            this.code = code == null ? "" : code; this.message = message;
        }
    }

    /** Validate and compute discount for a coupon code applied to a restaurant order. */
    public CouponResult apply(String code, String restaurantId, double subtotal) {
        if (code == null || code.isBlank()) return new CouponResult(true, 0, "", "");
        Coupon c = repo.findByCodeAndRestaurant(code, restaurantId);
        if (c == null) return new CouponResult(false, 0, code, "Invalid coupon code");
        if (!c.isEnabled()) return new CouponResult(false, 0, code, "Coupon is disabled");
        if (c.isExpired()) return new CouponResult(false, 0, code, "Coupon has expired");
        if (subtotal < c.getMinOrder())
            return new CouponResult(false, 0, code,
                    "Minimum order Rs. " + c.getMinOrder() + " required for this coupon");
        return new CouponResult(true, c.computeDiscount(subtotal), c.getCode(), "Coupon applied");
    }
}
