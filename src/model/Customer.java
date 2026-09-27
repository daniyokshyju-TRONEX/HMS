public class Customer extends Person {
    private String governmentId;

    public Customer() {
        super();
    }

    public Customer(int customerId, String fullName, String phoneNumber, String email,
                   String address, String governmentId) {
        super(customerId, fullName, phoneNumber, email, address);
        this.governmentId = governmentId;
    }

    public Customer(String fullName, String phoneNumber, String email,
                   String address, String governmentId) {
        super(0, fullName, phoneNumber, email, address);
        this.governmentId = governmentId;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    public void setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
    }

    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id + " | Name: " + fullName + " | Phone: " + phoneNumber);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", governmentId='" + governmentId + '\'' +
                '}';
    }
}
