package food_delivery_system.repository;

import food_delivery_system.model.Cart;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CartRepository {
    private static final String FILE = "cart.txt";
    @Autowired private FileUtil fileUtil;

    public List<Cart> findAll() {
        return fileUtil.readAllLines(FILE).stream().filter(l -> !l.isBlank())
                .map(this::parse).collect(Collectors.toList());
    }
    public List<Cart> findByCustomer(String cid) {
        return findAll().stream().filter(c -> cid.equals(c.getCustomerId())).collect(Collectors.toList());
    }
    public Cart findById(String id) {
        return findAll().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
    public Cart save(Cart c) {
        if (c.getId() == null || c.getId().isBlank()) c.setId("C-" + FileUtil.nextId());
        fileUtil.appendLine(FILE, toLine(c));
        return c;
    }
    public void update(Cart c) {
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
    public void clearForCustomer(String cid) {
        List<String> lines = findAll().stream().filter(c -> !cid.equals(c.getCustomerId()))
                .map(this::toLine).collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }
    private String toLine(Cart c) {
        return FileUtil.join(c.getId(), c.getCustomerId(), c.getFoodId(), c.getFoodName(),
                c.getRestaurantId(), c.getPrice(), c.getQuantity());
    }
    private Cart parse(String l) {
        String[] p = FileUtil.split(l);
        double price=0; int qty=1;
        try { price = Double.parseDouble(g(p,5)); } catch (Exception ignored) {}
        try { qty = Integer.parseInt(g(p,6)); } catch (Exception ignored) {}
        return new Cart(g(p,0),g(p,1),g(p,2),g(p,3),g(p,4),price,qty);
    }
    private static String g(String[] a, int i){ return i<a.length? a[i] : ""; }
}
