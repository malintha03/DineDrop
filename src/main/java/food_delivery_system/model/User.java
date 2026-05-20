package food_delivery_system.model;

/**
 * User model class
 * Model layer in MVC architecture
 * Encapsulation is implemented using private variables + getters/setters
 * Base class that can be inherited by Customer, Owner, Rider, or Admin classes
 * Role: CUSTOMER | OWNER | RIDER | ADMIN
 */
public class User {

    // Unique ID for each user
    private String id;

    // Stores user's full name
    private String name;

    // Stores login email
    private String email;

    // Stores user password
    // In real systems passwords should be encrypted
    private String password;

    // Stores phone number
    private String phone;

    // User role used for authorization
    private String role;

    // City field mainly for owners and riders
    private String city;

    // Vehicle type for delivery riders
    private String vehicle;

    // Rider identity/license number
    private String licenseNumber;

    // Vehicle registration number
    private String licensePlate;

    // Default constructor
    // Required for frameworks like Spring and JSON mapping
    public User() {}

    // Constructor overloading
    // Demonstrates Polymorphism concept in Java
    public User(String id, String name, String email, String password,
                String phone, String role, String city, String vehicle) {

        // Calls another constructor using this()
        // Constructor chaining improves code reusability
        this(id, name, email, password, phone, role, city, vehicle, "", "");
    }

    // Parameterized constructor
    // Used to initialize object values at object creation
    public User(String id, String name, String email, String password,
                String phone, String role, String city, String vehicle,
                String licenseNumber, String licensePlate) {

        // this keyword refers to current object variables
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.city = city;
        this.vehicle = vehicle;
        this.licenseNumber = licenseNumber;
        this.licensePlate = licensePlate;
    }

    // Getter method for id
    public String getId() {
        return id;
    }

    // Setter method for id
    public void setId(String id) {
        this.id = id;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter for phone number
    public String getPhone() {
        return phone;
    }

    // Setter for phone number
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter for role
    public String getRole() {
        return role;
    }

    // Setter for role
    public void setRole(String role) {
        this.role = role;
    }

    // Getter for city
    public String getCity() {
        return city;
    }

    // Setter for city
    public void setCity(String city) {
        this.city = city;
    }

    // Getter for vehicle
    public String getVehicle() {
        return vehicle;
    }

    // Setter for vehicle
    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    // Getter for rider license number
    public String getLicenseNumber() {
        return licenseNumber;
    }

    // Setter for rider license number
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    // Getter for vehicle plate number
    public String getLicensePlate() {
        return licensePlate;
    }

    // Setter for vehicle plate number
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

}