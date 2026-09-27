import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final RoomService roomService = new RoomService();
    private static final CustomerService customerService = new CustomerService();
    private static final ReservationService reservationService = new ReservationService();
    private static final PaymentService paymentService = new PaymentService();
    private static final ReportService reportService = new ReportService();

    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection().close();
        } catch (Exception e) {
            System.out.println("Database connection failed. Please create the MySQL database and update the credentials in DatabaseConnection.java.");
            System.out.println("Error: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = readInt("Choose an option: ", 1, 9);

            try {
                switch (choice) {
                    case 1 -> roomManagementMenu();
                    case 2 -> customerManagementMenu();
                    case 3 -> reservationManagementMenu();
                    case 4 -> handleCheckIn();
                    case 5 -> handleCheckOut();
                    case 6 -> paymentManagementMenu();
                    case 7 -> viewReports();
                    case 8 -> {
                        System.out.println("\nThank you for using Hotel Management System!");
                        running = false;
                    }
                    default -> System.out.println("Invalid menu choice.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
        SCANNER.close();
    }

    private static void showMainMenu() {
        System.out.println();
        System.out.println("# ========================================");
        System.out.println("HOTEL MANAGEMENT SYSTEM");
        System.out.println("# ========================================");
        System.out.println("1. Room Management");
        System.out.println("2. Customer Management");
        System.out.println("3. Reservation Management");
        System.out.println("4. Check-In");
        System.out.println("5. Check-Out");
        System.out.println("6. Payment Management");
        System.out.println("7. View Reports");
        System.out.println("8. Exit");
    }

    private static void roomManagementMenu() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("## ROOM MANAGEMENT");
            System.out.println("1. Add Room");
            System.out.println("2. View All Rooms");
            System.out.println("3. Search Room");
            System.out.println("4. View Available Rooms");
            System.out.println("5. Update Room");
            System.out.println("6. Delete Room");
            System.out.println("7. Back");

            int choice = readInt("Choose an option: ", 1, 7);
            switch (choice) {
                case 1 -> addRoom();
                case 2 -> displayRooms(roomService.getAllRooms());
                case 3 -> searchRoomByNumber();
                case 4 -> displayRooms(roomService.getAvailableRooms());
                case 5 -> updateRoom();
                case 6 -> deleteRoom();
                case 7 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addRoom() throws SQLException {
        int roomNumber = readInt("Enter room number: ", 1, Integer.MAX_VALUE);
        Room.RoomType roomType = chooseRoomType();
        double price = readDouble("Enter price per night: ");
        Room.AvailabilityStatus status = Room.AvailabilityStatus.AVAILABLE;

        Room room = new Room(roomNumber, roomType, price, status);
        roomService.addRoom(room);
        System.out.println("Room added successfully.");
    }

    private static void searchRoomByNumber() throws SQLException {
        int roomNumber = readInt("Enter room number: ", 1, Integer.MAX_VALUE);
        Room room = roomService.getRoomByNumber(roomNumber);
        if (room == null) {
            System.out.println("Room not found.");
        } else {
            displayRoom(room);
        }
    }

    private static void updateRoom() throws SQLException {
        int roomNumber = readInt("Enter room number to update: ", 1, Integer.MAX_VALUE);
        Room room = roomService.getRoomByNumber(roomNumber);
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        System.out.println("Current room details:");
        displayRoom(room);

        Room.RoomType newType = chooseRoomType();
        double newPrice = readDouble("Enter new price per night: ");
        Room.AvailabilityStatus newStatus = chooseAvailabilityStatus();

        room.setRoomType(newType);
        room.setPricePerNight(newPrice);
        room.setAvailabilityStatus(newStatus);
        roomService.updateRoom(room);
        System.out.println("Room updated successfully.");
    }

    private static void deleteRoom() throws SQLException {
        int roomNumber = readInt("Enter room number to delete: ", 1, Integer.MAX_VALUE);
        Room room = roomService.getRoomByNumber(roomNumber);
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        roomService.deleteRoom(roomNumber);
        System.out.println("Room deleted successfully.");
    }

    private static void customerManagementMenu() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("## CUSTOMER MANAGEMENT");
            System.out.println("1. Register Customer");
            System.out.println("2. View Customers");
            System.out.println("3. Search Customer by ID");
            System.out.println("4. Search Customer by Name");
            System.out.println("5. Update Customer");
            System.out.println("6. Delete Customer");
            System.out.println("7. Back");

            int choice = readInt("Choose an option: ", 1, 7);
            switch (choice) {
                case 1 -> registerCustomer();
                case 2 -> displayCustomers(customerService.getAllCustomers());
                case 3 -> searchCustomerById();
                case 4 -> searchCustomerByName();
                case 5 -> updateCustomer();
                case 6 -> deleteCustomer();
                case 7 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void registerCustomer() throws SQLException {
        String name = readNonEmptyString("Enter full name: ");
        String phone = readValidPhone();
        String email = readValidEmail();
        String address = readNonEmptyString("Enter address: ");
        String governmentId = readNonEmptyString("Enter government ID: ");

        Customer customer = new Customer(name, phone, email, address, governmentId);
        int generatedId = customerService.addCustomer(customer);
        System.out.println("Customer registered successfully with ID: " + generatedId);
    }

    private static void searchCustomerById() throws SQLException {
        int customerId = readInt("Enter customer ID: ", 1, Integer.MAX_VALUE);
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
        } else {
            displayCustomer(customer);
        }
    }

    private static void searchCustomerByName() throws SQLException {
        String name = readNonEmptyString("Enter customer name: ");
        List<Customer> customers = customerService.searchCustomerByName(name);
        if (customers.isEmpty()) {
            System.out.println("No customer found by that name.");
        } else {
            displayCustomers(customers);
        }
    }

    private static void updateCustomer() throws SQLException {
        int customerId = readInt("Enter customer ID to update: ", 1, Integer.MAX_VALUE);
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Current customer details:");
        displayCustomer(customer);

        customer.setFullName(readNonEmptyString("Enter new full name: "));
        customer.setPhoneNumber(readValidPhone());
        customer.setEmail(readValidEmail());
        customer.setAddress(readNonEmptyString("Enter new address: "));
        customer.setGovernmentId(readNonEmptyString("Enter new government ID: "));

        customerService.updateCustomer(customer);
        System.out.println("Customer updated successfully.");
    }

    private static void deleteCustomer() throws SQLException {
        int customerId = readInt("Enter customer ID to delete: ", 1, Integer.MAX_VALUE);
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        customerService.deleteCustomer(customerId);
        System.out.println("Customer deleted successfully.");
    }

    private static void reservationManagementMenu() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("## RESERVATION MANAGEMENT");
            System.out.println("1. Make Reservation");
            System.out.println("2. View Reservations");
            System.out.println("3. Search Reservation");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. Back");

            int choice = readInt("Choose an option: ", 1, 5);
            switch (choice) {
                case 1 -> makeReservation();
                case 2 -> displayReservations(reservationService.getAllReservations());
                case 3 -> searchReservationById();
                case 4 -> cancelReservation();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void makeReservation() throws SQLException {
        try {
            int customerId = readInt("Enter customer ID: ", 1, Integer.MAX_VALUE);
            int roomNumber = readInt("Enter room number: ", 1, Integer.MAX_VALUE);
            LocalDate checkIn = readDate("Enter check-in date (YYYY-MM-DD): ");
            LocalDate checkOut = readDate("Enter check-out date (YYYY-MM-DD): ");
            int guests = readInt("Enter number of guests: ", 1, Integer.MAX_VALUE);

            Reservation reservation = reservationService.createReservation(customerId, roomNumber, checkIn, checkOut, guests);
            System.out.println("Reservation created successfully. Reservation ID: " + reservation.getReservationId());
            System.out.println("Estimated total amount: " + reservation.getTotalAmount());
        } catch (CustomerNotFoundException | RoomNotAvailableException | InvalidDateException ex) {
            System.out.println("Reservation failed: " + ex.getMessage());
        }
    }

    private static void searchReservationById() throws SQLException {
        int reservationId = readInt("Enter reservation ID: ", 1, Integer.MAX_VALUE);
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            System.out.println("Reservation not found.");
        } else {
            displayReservation(reservation);
        }
    }

    private static void cancelReservation() throws SQLException {
        int reservationId = readInt("Enter reservation ID to cancel: ", 1, Integer.MAX_VALUE);
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            System.out.println("Reservation not found.");
            return;
        }

        reservationService.cancelReservation(reservationId);
        System.out.println("Reservation cancelled successfully.");
    }

    private static void handleCheckIn() throws SQLException {
        try {
            int reservationId = readInt("Enter reservation ID to check in: ", 1, Integer.MAX_VALUE);
            Reservation reservation = reservationService.checkInReservation(reservationId);
            System.out.println("Check-in successful for reservation ID: " + reservation.getReservationId());
        } catch (ReservationNotFoundException ex) {
            System.out.println("Check-in failed: " + ex.getMessage());
        }
    }

    private static void handleCheckOut() throws SQLException {
        try {
            int reservationId = readInt("Enter reservation ID to check out: ", 1, Integer.MAX_VALUE);
            Reservation reservation = reservationService.checkOutReservation(reservationId);
            Room room = roomService.getRoomByNumber(reservation.getRoomNumber());
            if (room != null) {
                printHotelBill(reservation, room);
            }
            System.out.println("Check-out successful for reservation ID: " + reservation.getReservationId());
        } catch (ReservationNotFoundException ex) {
            System.out.println("Check-out failed: " + ex.getMessage());
        }
    }

    private static void paymentManagementMenu() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("## PAYMENT MANAGEMENT");
            System.out.println("1. Record Payment");
            System.out.println("2. View All Payments");
            System.out.println("3. View Payment by Reservation");
            System.out.println("4. Back");

            int choice = readInt("Choose an option: ", 1, 4);
            switch (choice) {
                case 1 -> recordPayment();
                case 2 -> displayPayments(paymentService.getAllPayments());
                case 3 -> viewPaymentByReservation();
                case 4 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void recordPayment() throws SQLException {
        try {
            int reservationId = readInt("Enter reservation ID: ", 1, Integer.MAX_VALUE);
            double amount = readDouble("Enter amount: ");
            Payment.PaymentMethod method = choosePaymentMethod();

            Payment payment = paymentService.createPayment(reservationId, amount, method.name());
            System.out.println("Payment recorded successfully. Payment ID: " + payment.getPaymentId());
        } catch (ReservationNotFoundException ex) {
            System.out.println("Payment failed: " + ex.getMessage());
        }
    }

    private static void viewPaymentByReservation() throws SQLException {
        int reservationId = readInt("Enter reservation ID: ", 1, Integer.MAX_VALUE);
        Payment payment = paymentService.getPaymentByReservationId(reservationId);
        if (payment == null) {
            System.out.println("No payment found for this reservation.");
        } else {
            System.out.println(payment);
        }
    }

    private static void viewReports() throws SQLException {
        Map<String, Long> summary = reportService.getSummary();
        System.out.println();
        System.out.println("## REPORT SUMMARY");
        System.out.println("Total rooms: " + summary.getOrDefault("totalRooms", 0L));
        System.out.println("Available rooms: " + summary.getOrDefault("availableRooms", 0L));
        System.out.println("Occupied rooms: " + summary.getOrDefault("occupiedRooms", 0L));
        System.out.println("Total customers: " + summary.getOrDefault("totalCustomers", 0L));
        System.out.println("Active reservations: " + summary.getOrDefault("activeReservations", 0L));
        System.out.println("Completed reservations: " + summary.getOrDefault("completedReservations", 0L));
        System.out.println("Total payments: " + summary.getOrDefault("totalPayments", 0L));
    }

    private static Room.RoomType chooseRoomType() {
        System.out.println("Room types:");
        System.out.println("1. SINGLE");
        System.out.println("2. DOUBLE");
        System.out.println("3. DELUXE");
        System.out.println("4. SUITE");
        int choice = readInt("Select room type: ", 1, 4);
        return switch (choice) {
            case 1 -> Room.RoomType.SINGLE;
            case 2 -> Room.RoomType.DOUBLE;
            case 3 -> Room.RoomType.DELUXE;
            case 4 -> Room.RoomType.SUITE;
            default -> Room.RoomType.SINGLE;
        };
    }

    private static Room.AvailabilityStatus chooseAvailabilityStatus() {
        System.out.println("Availability statuses:");
        System.out.println("1. AVAILABLE");
        System.out.println("2. OCCUPIED");
        System.out.println("3. MAINTENANCE");
        int choice = readInt("Select availability: ", 1, 3);
        return switch (choice) {
            case 1 -> Room.AvailabilityStatus.AVAILABLE;
            case 2 -> Room.AvailabilityStatus.OCCUPIED;
            case 3 -> Room.AvailabilityStatus.MAINTENANCE;
            default -> Room.AvailabilityStatus.AVAILABLE;
        };
    }

    private static Payment.PaymentMethod choosePaymentMethod() {
        System.out.println("Payment methods:");
        System.out.println("1. CASH");
        System.out.println("2. CARD");
        System.out.println("3. UPI");
        int choice = readInt("Select payment method: ", 1, 3);
        return switch (choice) {
            case 1 -> Payment.PaymentMethod.CASH;
            case 2 -> Payment.PaymentMethod.CARD;
            case 3 -> Payment.PaymentMethod.UPI;
            default -> Payment.PaymentMethod.CASH;
        };
    }

    private static void displayRoom(Room room) {
        System.out.println("Room Number: " + room.getRoomNumber());
        System.out.println("Type: " + room.getRoomType());
        System.out.println("Price Per Night: " + room.getPricePerNight());
        System.out.println("Availability: " + room.getAvailabilityStatus());
    }

    private static void displayRooms(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }
        for (Room room : rooms) {
            displayRoom(room);
            System.out.println("----------------------------------------");
        }
    }

    private static void displayCustomer(Customer customer) {
        System.out.println("Customer ID: " + customer.getId());
        System.out.println("Name: " + customer.getFullName());
        System.out.println("Phone: " + customer.getPhoneNumber());
        System.out.println("Email: " + customer.getEmail());
        System.out.println("Address: " + customer.getAddress());
        System.out.println("Government ID: " + customer.getGovernmentId());
    }

    private static void displayCustomers(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer customer : customers) {
            displayCustomer(customer);
            System.out.println("----------------------------------------");
        }
    }

    private static void displayReservation(Reservation reservation) {
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Customer ID: " + reservation.getCustomerId());
        System.out.println("Room Number: " + reservation.getRoomNumber());
        System.out.println("Check-in: " + reservation.getCheckInDate());
        System.out.println("Check-out: " + reservation.getCheckOutDate());
        System.out.println("Guests: " + reservation.getNumberOfGuests());
        System.out.println("Status: " + reservation.getReservationStatus());
        System.out.println("Total Amount: " + reservation.getTotalAmount());
    }

    private static void displayReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        for (Reservation reservation : reservations) {
            displayReservation(reservation);
            System.out.println("----------------------------------------");
        }
    }

    private static void displayPayment(Payment payment) {
        System.out.println("Payment ID: " + payment.getPaymentId());
        System.out.println("Reservation ID: " + payment.getReservationId());
        System.out.println("Amount: " + payment.getAmount());
        System.out.println("Payment Date: " + payment.getPaymentDate());
        System.out.println("Method: " + payment.getPaymentMethod());
        System.out.println("Status: " + payment.getPaymentStatus());
    }

    private static void displayPayments(List<Payment> payments) {
        if (payments.isEmpty()) {
            System.out.println("No payments found.");
            return;
        }
        for (Payment payment : payments) {
            displayPayment(payment);
            System.out.println("----------------------------------------");
        }
    }

    private static void printHotelBill(Reservation reservation, Room room) throws SQLException {
        long nights = reservation.getNumberOfNights();
        double total = nights * room.getPricePerNight();
        Payment payment = paymentService.getPaymentByReservationId(reservation.getReservationId());

        System.out.println("\n---");
        System.out.println("## HOTEL BILL");
        System.out.println("Reservation ID: " + reservation.getReservationId());
        Customer customer = customerService.getCustomerById(reservation.getCustomerId());
        System.out.println("Customer: " + (customer != null ? customer.getFullName() : "Unknown"));
        System.out.println("Room: " + room.getRoomNumber());
        System.out.println("Room Type: " + room.getRoomType());
        System.out.println("Check-in: " + reservation.getCheckInDate());
        System.out.println("Check-out: " + reservation.getCheckOutDate());
        System.out.println("Number of Nights: " + nights);
        System.out.println("Price per Night: " + room.getPricePerNight());
        System.out.println("Total Amount: " + total);
        System.out.println("Payment Status: " + (payment != null ? payment.getPaymentStatus() : "PENDING"));
        System.out.println("---");
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String text = SCANNER.nextLine();
            try {
                int value = Integer.parseInt(text.trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a valid number between " + min + " and " + max + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = SCANNER.nextLine();
            try {
                double value = Double.parseDouble(text.trim());
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid positive value.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = SCANNER.nextLine().trim();
            try {
                return LocalDate.parse(text);
            } catch (Exception ex) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    private static String readValidPhone() {
        while (true) {
            String phone = readNonEmptyString("Enter phone number: ");
            if (phone.matches("^[0-9+()\\-\\s]{10,20}$")) {
                return phone;
            }
            System.out.println("Invalid phone number. Use 10 to 20 digits with optional +, spaces, parentheses or hyphen.");
        }
    }

    private static String readValidEmail() {
        while (true) {
            String email = readNonEmptyString("Enter email: ");
            if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return email;
            }
            System.out.println("Invalid email format.");
        }
    }
}
