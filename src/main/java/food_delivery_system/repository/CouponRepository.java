package food_delivery_system.repository;

import food_delivery_system.model.Coupon;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CouponRepository {
    private static final String FILE = "coupons.txt";
    @Autowired private FileUtil fileUtil;

    public List<Coupon> findAll() {
        return fileUtil.readAllLines(FILE).stream().filter(l -> !l.isBlank())
                .map(this::parse).collect(Collectors.toList());
    }
    public Coupon findById(String id) {
        return findAll().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
    public List<Coupon> findByRestaurant(String rid) {
        return findAll().stream().filter(c -> rid.equals(c.getRestaurantId())).collect(Collectors.toList());
    }
    public Coupon findByCodeAndRestaurant(String code, String restaurantId) {
        if (code == null) return null;
        String c = code.trim().toUpperCase();
        return findAll().stream()
                .filter(x -> x.getCode().equals(c) && x.getRestaurantId().equals(restaurantId))
                .findFirst().orElse(null);
    }
    public Coupon save(Coupon c) {
        if (c.getId() == null || c.getId().isBlank()) c.setId("CP-" + FileUtil.nextId());
        fileUtil.appendLine(FILE, toLine(c));
        return c;
    }
    public void update(Coupon c) {
        List<String> lines = findAll().stream()
                .map(x -> toLine(x.getId().equals(c.getId()) ? c : x))
                .collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }
    public void delete(String id) {
        List<String> lines = findAll().stream().filter(c -> !c.getId().equals(id))
                .map(this::toLine).collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }

    private String toLine(Coupon c) {
        return FileUtil.join(c.getId(), c.getRestaurantId(), c.getCode(), c.getType(),
                c.getValue(), c.getMinOrder(), c.getExpiryDate(),
                c.isEnabled() ? "1" : "0", c.getDescription());
    }
    private Coupon parse(String l) {
        String[] p = FileUtil.split(l);
        double v=0, m=0;
        try{v=Double.parseDouble(g(p,4));}catch(Exception ignored){}
        try{m=Double.parseDouble(g(p,5));}catch(Exception ignored){}
        boolean enabled = "1".equals(g(p,7)) || "true".equalsIgnoreCase(g(p,7));
        return new Coupon(g(p,0), g(p,1), g(p,2), g(p,3), v, m, g(p,6), enabled, g(p,8));
    }
    private static String g(String[] a, int i){ return i<a.length ? a[i] : ""; }
}
