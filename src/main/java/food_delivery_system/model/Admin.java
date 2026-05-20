package food_delivery_system.model;

/** * Admin — demonstrates Inheritance from User.
 * OOP Concept: Inheritance (using 'extends' keyword).
 * Admin inherits attributes (id, name, email) and behaviors from the User class.
 */
public class Admin extends User {

    // Default Constructor: Used for creating an empty Admin object.
    public Admin() {
        super(); // Calls the parent (User) class constructor.
        setRole("ADMIN"); // Encapsulation: Modifying internal state via a setter.
    }

    // Parameterized Constructor: Used for object initialization with data.
    // Polymorphism: Constructor overloading (if multiple constructors exist in User).
    public Admin(String id, String name, String email, String password) {
        // super() passes arguments to the parent class constructor to reuse code.
        super(id, name, email, password, "", "ADMIN", "", "");
    }
}

