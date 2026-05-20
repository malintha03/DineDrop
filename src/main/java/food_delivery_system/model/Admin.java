package food_delivery_system.model;

/** Admin — demonstrates Inheritance from User. */
public class Admin extends User {
    public Admin() { super(); setRole("ADMIN"); }
    public Admin(String id, String name, String email, String password) {
        super(id, name, email, password, "", "ADMIN", "", "");
    }
}
