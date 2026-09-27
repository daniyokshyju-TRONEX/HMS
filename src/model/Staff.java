public class Staff extends Person {
    private int staffId;
    private String role;
    private String username;
    private String passwordHash;

    public Staff() {
        super();
    }

    public Staff(int staffId, String fullName, String phoneNumber, String email,
                String address, String role, String username, String passwordHash) {
        super(staffId, fullName, phoneNumber, email, address);
        this.staffId = staffId;
        this.role = role;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Staff(String fullName, String phoneNumber, String email, String address,
                String role, String username, String passwordHash) {
        super(0, fullName, phoneNumber, email, address);
        this.role = role;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
        this.id = staffId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public void displayInfo() {
        System.out.println("Staff ID: " + staffId + " | Name: " + fullName + " | Role: " + role);
    }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId=" + staffId +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
