package food_delivery_system.repository;

import food_delivery_system.model.Order;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
    private static final String FILE = "orders.txt";
    @Autowired private FileUtil fileUtil;

    public List<Order> findAll() {
        return fileUtil.readAllLines(FILE).stream().filter(l -> !l.isBlank())
                .map(this::parse).collect(Collectors.toList());
    }
    public Order findById(String id) {
        return findAll().stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
    }
    public List<Order> findByCustomer(String cid) {
        return findAll().stream().filter(o -> cid.equals(o.getCustomerId())).collect(Collectors.toList());
    }
    public List<Order> findByRestaurant(String rid) {
        return findAll().stream().filter(o -> rid.equals(o.getRestaurantId())).collect(Collectors.toList());
    }
    public List<Order> findByRider(String rid) {
        return findAll().stream().filter(o -> rid.equals(o.getRiderId())).collect(Collectors.toList());
    }
    public List<Order> findUnassigned() {
        return findAll().stream().filter(o -> (o.getRiderId()==null||o.getRiderId().isBlank())
                        && !"DELIVERED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .collect(Collectors.toList());
    }
    public Order save(Order o) {
        if (o.getId()==null||o.getId().isBlank()) o.setId("O-" + FileUtil.nextId());
        fileUtil.appendLine(FILE, toLine(o));
        return o;
    }
    public void update(Order o) {
        List<String> lines = findAll().stream()
                .map(x -> toLine(x.getId().equals(o.getId()) ? o : x))
                .collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }
    public void delete(String id) {
        List<String> lines = findAll().stream().filter(o -> !o.getId().equals(id))
                .map(this::toLine).collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }
    private String toLine(Order o) {
        return FileUtil.join(o.getId(), o.getCustomerId(), o.getRestaurantId(), o.getItems(),
                o.getSubtotal(), o.getDeliveryFee(), o.getTotal(), o.getAddress(), o.getCity(),
                o.getStatus(), o.getRiderId(), o.getCreatedAt(),
                o.getCustomerLatitude(), o.getCustomerLongitude(),
                o.getRestaurantLatitude(), o.getRestaurantLongitude(),
                o.getRestaurantAddress(), o.getRestaurantCity(),
                // v2 fields (positions 18-23)
                o.getFoodCost(), o.getWebsiteCommission(), o.getRiderWebsiteFee(),
                o.getRiderEarning(), o.getDiscount(), o.getCouponCode(),
                // v3 field (position 24)
                o.getCompletedAt());
    }
    private Order parse(String l) {
        String[] p = FileUtil.split(l);
        double sub=0, fee=0, tot=0, foodCost=0, wc=0, rwf=0, re=0, disc=0;
        try{sub=Double.parseDouble(g(p,4));}catch(Exception ignored){}
        try{fee=Double.parseDouble(g(p,5));}catch(Exception ignored){}
        try{tot=Double.parseDouble(g(p,6));}catch(Exception ignored){}
        try{foodCost=Double.parseDouble(g(p,18));}catch(Exception ignored){}
        try{wc=Double.parseDouble(g(p,19));}catch(Exception ignored){}
        try{rwf=Double.parseDouble(g(p,20));}catch(Exception ignored){}
        try{re=Double.parseDouble(g(p,21));}catch(Exception ignored){}
        try{disc=Double.parseDouble(g(p,22));}catch(Exception ignored){}
        Order o = new Order(g(p,0),g(p,1),g(p,2),g(p,3),sub,fee,tot,g(p,7),g(p,8),g(p,9),g(p,10),g(p,11),
                g(p,12),g(p,13),g(p,14),g(p,15),g(p,16),g(p,17),
                foodCost, wc, rwf, re, disc, g(p,23));
        o.setCompletedAt(g(p,24));
        return o;
    }
    private static String g(String[] a, int i){ return i<a.length? a[i] : ""; }
}
