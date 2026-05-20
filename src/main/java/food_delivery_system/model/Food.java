package food_delivery_system.model;

// Model class represents a Food entity in MVC architecture
// This class stores food item data for the system

/** Food item in a restaurant menu. */
public class Food {

    // Encapsulation:
    // Private variables protect data from direct access

    // Unique ID for each food item
    private String id;

    // Stores related restaurant ID
    // Relationship: aggregation between Food and Restaurant
    private String restaurantId;

    // Food item name
    private String name;

    // Food category (Example: Pizza, Burger, Drinks)
    private String category;

    // Food price
    private double price;

    // Stores uploaded image filename/path
    private String image;

    // Food description/details
    private String description;

    // Default constructor
    // Required for frameworks like Spring and Jackson
    public Food() {}

    // Parameterized constructor
    // Used to initialize Food object with values
    public Food(String id, String restaurantId, String name, String category,
                double price, String image, String description) {

        //  refers to current object variables
        this.id=id;
        this.restaurantId=restaurantId;
        this.name=name;
        this.category=category;
        this.price=price;
        this.image=image;
        this.description=description;
    }

    // Getter method returns food ID
    public String getId(){return id;}

    // Setter method updates food ID
    public void setId(String id){this.id=id;}

    // Getter method for restaurant ID
    public String getRestaurantId(){return restaurantId;}

    // Setter method for restaurant ID
    public void setRestaurantId(String r){this.restaurantId=r;}

    // Getter method for food name
    public String getName(){return name;}

    // Setter method for food name
    public void setName(String n){this.name=n;}

    // Getter method for category
    public String getCategory(){return category;}

    // Setter method for category
    public void setCategory(String c){this.category=c;}

    // Getter method for price
    public double getPrice(){return price;}

    // Setter method for price
    public void setPrice(double p){this.price=p;}

    // Getter method for image
    public String getImage(){return image;}

    // Setter method for image
    public void setImage(String i){this.image=i;}

    // Getter method for description
    public String getDescription(){return description;}

    // Setter method for description
    public void setDescription(String d){this.description=d;}

}