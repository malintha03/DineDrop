package food_delivery_system.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Restaurant model. */
public class Restaurant {
    private String id;
    private String ownerId;
    private String name;
    private String city;
    private String address;
    private String cuisine;
    private String image; // file name in /uploads or /static/images
    private String description;
    private String latitude;
    private String longitude;

    public Restaurant() {}

    public Restaurant(String id, String ownerId, String name, String city, String address,
                      String cuisine, String image, String description) {
        this(id, ownerId, name, city, address, cuisine, image, description, "", "");
    }

    public Restaurant(String id, String ownerId, String name, String city, String address,
                      String cuisine, String image, String description,
                      String latitude, String longitude) {
        this.id=id; this.ownerId=ownerId; this.name=name; this.city=city;
        this.address=address; this.cuisine=cuisine; this.image=image; this.description=description;
        this.latitude = latitude == null ? "" : latitude;
        this.longitude = longitude == null ? "" : longitude;
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; } public void setOwnerId(String o) { this.ownerId = o; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public String getCity() { return city; } public void setCity(String c) { this.city = c; }
    public String getAddress() { return address; } public void setAddress(String a) { this.address = a; }
    public String getCuisine() { return cuisine; } public void setCuisine(String c) { this.cuisine = c; }
    public String getImage() { return image; } public void setImage(String i) { this.image = i; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getLatitude() { return latitude; } public void setLatitude(String latitude) { this.latitude = latitude == null ? "" : latitude; }
    public String getLongitude() { return longitude; } public void setLongitude(String longitude) { this.longitude = longitude == null ? "" : longitude; }

    public boolean hasCoordinates() {
        return latitude != null && !latitude.isBlank() && longitude != null && !longitude.isBlank();
    }

    public String getMapQuery() {
        if (hasCoordinates()) return latitude + "," + longitude;
        String q = ((address == null ? "" : address) + ", " + (city == null ? "" : city)).trim();
        return q.isBlank() || q.equals(",") ? "Sri Lanka" : q;
    }

    private String encodedMapQuery() {
        return URLEncoder.encode(getMapQuery(), StandardCharsets.UTF_8);
    }

    public String getMapEmbedUrl() {
        return "https://maps.google.com/maps?q=" + encodedMapQuery() + "&output=embed";
    }

    public String getGoogleMapsUrl() {
        return "https://www.google.com/maps/search/?api=1&query=" + encodedMapQuery();
    }
}
