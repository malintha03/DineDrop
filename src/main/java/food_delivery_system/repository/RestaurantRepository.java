package food_delivery_system.repository;

import food_delivery_system.model.Restaurant;
import food_delivery_system.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RestaurantRepository {
    private static final String FILE = "restaurants.txt";
    @Autowired private FileUtil fileUtil;

    public List<Restaurant> findAll() {
        return fileUtil.readAllLines(FILE).stream().filter(l -> !l.isBlank())
                .map(this::parse).collect(Collectors.toList());
    }
    public Restaurant findById(String id) {
        return findAll().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }
    public List<Restaurant> findByOwner(String ownerId) {
        return findAll().stream().filter(r -> ownerId.equals(r.getOwnerId())).collect(Collectors.toList());
    }
    public Restaurant save(Restaurant r) {
        if (r.getId() == null || r.getId().isBlank()) r.setId("R-" + FileUtil.nextId());
        fileUtil.appendLine(FILE, toLine(r));
        return r;
    }
    public void update(Restaurant r) {
        List<String> lines = findAll().stream()
                .map(x -> toLine(x.getId().equals(r.getId()) ? r : x))
                .collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }
    public void delete(String id) {
        List<String> lines = findAll().stream().filter(r -> !r.getId().equals(id))
                .map(this::toLine).collect(Collectors.toList());
        fileUtil.writeAllLines(FILE, lines);
    }
    private String toLine(Restaurant r) {
        return FileUtil.join(r.getId(), r.getOwnerId(), r.getName(), r.getCity(),
                r.getAddress(), r.getCuisine(), r.getImage(), r.getDescription(),
                r.getLatitude(), r.getLongitude());
    }
    private Restaurant parse(String l) {
        String[] p = FileUtil.split(l);
        return new Restaurant(g(p,0),g(p,1),g(p,2),g(p,3),g(p,4),g(p,5),g(p,6),g(p,7),g(p,8),g(p,9));
    }
    private static String g(String[] a, int i){ return i<a.length? a[i] : ""; }
}
