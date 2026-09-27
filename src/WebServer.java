import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {
    private static final int PORT = 8080;
    private static final Path WEB_ROOT = Path.of("web").toAbsolutePath();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", WebServer::handleStaticFile);
        server.createContext("/api/rooms", WebServer::handleRooms);
        server.createContext("/api/customers", WebServer::handleCustomers);
        server.createContext("/api/reservations", WebServer::handleReservations);
        server.createContext("/api/checkin", WebServer::handleCheckIn);
        server.createContext("/api/checkout", WebServer::handleCheckOut);
        server.createContext("/api/payments", WebServer::handlePayments);
        server.createContext("/api/bill", WebServer::handleBill);
        server.createContext("/api/reports", WebServer::handleReports);
        server.setExecutor(null);
        server.start();
        System.out.println("Hotel Management Web Server running at http://localhost:" + PORT);
    }

    private static void handleStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || "/".equals(path)) {
            path = "/index.html";
        }

        if (path.startsWith("/api/")) {
            sendText(exchange, 404, "Not found", "text/plain; charset=UTF-8");
            return;
        }

        String relative = path.startsWith("/") ? path.substring(1) : path;
        Path file = WEB_ROOT.resolve(relative).normalize();

        if (!file.startsWith(WEB_ROOT) || !Files.exists(file) || !Files.isRegularFile(file)) {
            file = WEB_ROOT.resolve("index.html");
        }

        String contentType = getContentType(file);
        byte[] data = Files.readAllBytes(file);
        sendBytes(exchange, 200, data, contentType);
    }

    private static void handleRooms(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryString(exchange.getRequestURI().getRawQuery());
                String roomNumberParam = params.get("roomNumber");
                String availableParam = params.get("available");

                if (roomNumberParam != null && !roomNumberParam.isBlank()) {
                    Room room = new RoomService().getRoomByNumber(Integer.parseInt(roomNumberParam));
                    sendJson(exchange, 200, room == null ? "[]" : "[" + roomToJson(room) + "]");
                    return;
                }

                if ("true".equalsIgnoreCase(availableParam)) {
                    sendJson(exchange, 200, roomsToJson(new RoomService().getAvailableRooms()));
                    return;
                }

                List<Room> rooms = new RoomService().getAllRooms();
                sendJson(exchange, 200, roomsToJson(rooms));
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                int roomNumber = Integer.parseInt(form.getOrDefault("roomNumber", "0"));
                Room.RoomType roomType = Room.RoomType.valueOf(form.getOrDefault("roomType", "SINGLE").toUpperCase());
                double pricePerNight = Double.parseDouble(form.getOrDefault("pricePerNight", "0"));

                Room room = new Room(roomNumber, roomType, pricePerNight, Room.AvailabilityStatus.AVAILABLE);
                new RoomService().addRoom(room);

                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Room added successfully\"}");
                return;
            }

            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                int roomNumber = Integer.parseInt(form.getOrDefault("roomNumber", "0"));
                new RoomService().deleteRoom(roomNumber);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Room deleted successfully\"}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleCustomers(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryString(exchange.getRequestURI().getRawQuery());
                String customerIdParam = params.get("customerId");
                String nameParam = params.get("name");

                if (customerIdParam != null && !customerIdParam.isBlank()) {
                    Customer customer = new CustomerService().getCustomerById(Integer.parseInt(customerIdParam));
                    sendJson(exchange, 200, customer == null ? "[]" : "[" + customerToJson(customer) + "]");
                    return;
                }

                if (nameParam != null && !nameParam.isBlank()) {
                    List<Customer> customers = new CustomerService().searchCustomerByName(nameParam);
                    sendJson(exchange, 200, customersToJson(customers));
                    return;
                }

                List<Customer> customers = new CustomerService().getAllCustomers();
                sendJson(exchange, 200, customersToJson(customers));
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                Customer customer = new Customer(
                        form.getOrDefault("fullName", ""),
                        form.getOrDefault("phoneNumber", ""),
                        form.getOrDefault("email", ""),
                        form.getOrDefault("address", ""),
                        form.getOrDefault("governmentId", "")
                );

                int customerId = new CustomerService().addCustomer(customer);
                sendJson(exchange, 200, "{\"success\":true,\"customerId\":" + customerId + ",\"message\":\"Customer added successfully\"}");
                return;
            }

            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                int customerId = Integer.parseInt(form.getOrDefault("customerId", "0"));
                new CustomerService().deleteCustomer(customerId);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Customer deleted successfully\"}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleReservations(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryString(exchange.getRequestURI().getRawQuery());
                String reservationIdParam = params.get("reservationId");
                if (reservationIdParam != null && !reservationIdParam.isBlank()) {
                    Reservation reservation = new ReservationService().getReservationById(Integer.parseInt(reservationIdParam));
                    sendJson(exchange, 200, reservation == null ? "[]" : "[" + reservationToJson(reservation) + "]");
                    return;
                }

                List<Reservation> reservations = new ReservationService().getAllReservations();
                sendJson(exchange, 200, reservationsToJson(reservations));
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                int customerId = Integer.parseInt(form.getOrDefault("customerId", "0"));
                int roomNumber = Integer.parseInt(form.getOrDefault("roomNumber", "0"));
                LocalDate checkInDate = LocalDate.parse(form.getOrDefault("checkInDate", LocalDate.now().toString()));
                LocalDate checkOutDate = LocalDate.parse(form.getOrDefault("checkOutDate", LocalDate.now().plusDays(1).toString()));
                int numberOfGuests = Integer.parseInt(form.getOrDefault("numberOfGuests", "1"));

                Reservation reservation = new ReservationService().createReservation(customerId, roomNumber, checkInDate, checkOutDate, numberOfGuests);
                sendJson(exchange, 200, "{\"success\":true,\"reservationId\":" + reservation.getReservationId() + ",\"estimatedTotal\":" + reservation.getTotalAmount() + ",\"message\":\"Reservation created successfully\"}");
                return;
            }

            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                int reservationId = Integer.parseInt(form.getOrDefault("reservationId", "0"));
                new ReservationService().cancelReservation(reservationId);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Reservation cancelled successfully\"}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleReports(HttpExchange exchange) throws IOException {
        try {
            Map<String, Long> report = new ReportService().getSummary();
            String json = "{\n"
                    + "\"totalRooms\":" + report.getOrDefault("totalRooms", 0L) + ",\n"
                    + "\"availableRooms\":" + report.getOrDefault("availableRooms", 0L) + ",\n"
                    + "\"occupiedRooms\":" + report.getOrDefault("occupiedRooms", 0L) + ",\n"
                    + "\"totalCustomers\":" + report.getOrDefault("totalCustomers", 0L) + ",\n"
                    + "\"activeReservations\":" + report.getOrDefault("activeReservations", 0L) + ",\n"
                    + "\"completedReservations\":" + report.getOrDefault("completedReservations", 0L) + ",\n"
                    + "\"totalPayments\":" + report.getOrDefault("totalPayments", 0L) + "\n}";
            sendJson(exchange, 200, json);
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    private static void handleCheckIn(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> form = parseForm(exchange);
            int reservationId = Integer.parseInt(form.getOrDefault("reservationId", "0"));
            Reservation reservation = new ReservationService().checkInReservation(reservationId);
            sendJson(exchange, 200, "{\"success\":true,\"reservationId\":" + reservation.getReservationId() + ",\"message\":\"Reservation checked in successfully\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleCheckOut(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> form = parseForm(exchange);
            int reservationId = Integer.parseInt(form.getOrDefault("reservationId", "0"));
            Reservation reservation = new ReservationService().checkOutReservation(reservationId);
            double bill = new ReservationService().calculateBill(reservationId);
            sendJson(exchange, 200, "{\"success\":true,\"reservationId\":" + reservation.getReservationId() + ",\"bill\":" + String.format("%.2f", bill) + ",\"message\":\"Reservation checked out successfully\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handlePayments(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Payment> payments = new PaymentService().getAllPayments();
                sendJson(exchange, 200, paymentsToJson(payments));
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                int reservationId = Integer.parseInt(form.getOrDefault("reservationId", "0"));
                double amount = Double.parseDouble(form.getOrDefault("amount", "0"));
                String method = form.getOrDefault("paymentMethod", "CASH");
                Payment payment = new PaymentService().createPayment(reservationId, amount, method);
                sendJson(exchange, 200, "{\"success\":true,\"paymentId\":" + payment.getPaymentId() + ",\"message\":\"Payment recorded successfully\"}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleBill(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> parameters = parseQueryString(exchange.getRequestURI().getRawQuery());
            int reservationId = Integer.parseInt(parameters.getOrDefault("reservationId", "0"));
            Reservation reservation = new ReservationService().getReservationById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation not found.");
            }

            Customer customer = new CustomerService().getCustomerById(reservation.getCustomerId());
            Room room = new RoomService().getRoomByNumber(reservation.getRoomNumber());
            Payment payment = new PaymentService().getPaymentByReservationId(reservationId);
            String paymentStatus = payment == null ? "PENDING" : payment.getPaymentStatus().name();
            double finalBill = reservation.getNumberOfNights() * room.getPricePerNight();

            String json = "{\n"
                    + "\"reservationId\":" + reservation.getReservationId() + ",\n"
                    + "\"customer\":\"" + safeJson(customer.getFullName()) + "\",\n"
                    + "\"room\":" + reservation.getRoomNumber() + ",\n"
                    + "\"roomType\":\"" + room.getRoomType() + "\",\n"
                    + "\"checkIn\":\"" + reservation.getCheckInDate() + "\",\n"
                    + "\"checkOut\":\"" + reservation.getCheckOutDate() + "\",\n"
                    + "\"numberOfNights\":" + reservation.getNumberOfNights() + ",\n"
                    + "\"pricePerNight\":" + String.format("%.2f", room.getPricePerNight()) + ",\n"
                    + "\"totalAmount\":" + String.format("%.2f", finalBill) + ",\n"
                    + "\"paymentStatus\":\"" + paymentStatus + "\"\n}";
            sendJson(exchange, 200, json);
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"error\":\"" + safeJson(e.getMessage()) + "\"}");
        }
    }

    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        byte[] raw = exchange.getRequestBody().readAllBytes();
        String text = new String(raw, StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();

        if (text.isEmpty()) {
            return form;
        }

        for (String pair : text.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                form.put(key, value);
            }
        }
        return form;
    }

    private static String roomToJson(Room room) {
        return "{\"roomNumber\":" + room.getRoomNumber()
            + ",\"roomType\":\"" + room.getRoomType() + "\""
            + ",\"pricePerNight\":" + String.format("%.2f", room.getPricePerNight())
            + ",\"availabilityStatus\":\"" + room.getAvailabilityStatus() + "\"}";
    }

    private static String customerToJson(Customer customer) {
        return "{\"customerId\":" + customer.getId()
            + ",\"fullName\":\"" + safeJson(customer.getFullName()) + "\""
            + ",\"phoneNumber\":\"" + safeJson(customer.getPhoneNumber()) + "\""
            + ",\"email\":\"" + safeJson(customer.getEmail()) + "\""
            + ",\"address\":\"" + safeJson(customer.getAddress()) + "\""
            + ",\"governmentId\":\"" + safeJson(customer.getGovernmentId()) + "\"}";
    }

    private static String reservationToJson(Reservation reservation) {
        return "{\"reservationId\":" + reservation.getReservationId()
            + ",\"customerId\":" + reservation.getCustomerId()
            + ",\"roomNumber\":" + reservation.getRoomNumber()
            + ",\"checkInDate\":\"" + reservation.getCheckInDate() + "\""
            + ",\"checkOutDate\":\"" + reservation.getCheckOutDate() + "\""
            + ",\"numberOfGuests\":" + reservation.getNumberOfGuests()
            + ",\"status\":\"" + reservation.getReservationStatus() + "\""
            + ",\"totalAmount\":" + String.format("%.2f", reservation.getTotalAmount()) + "}";
    }

    private static String roomsToJson(List<Room> rooms) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (i > 0) sb.append(',');
            sb.append(roomToJson(room));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String customersToJson(List<Customer> customers) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"customerId\":").append(customer.getId())
              .append(",\"fullName\":\"").append(safeJson(customer.getFullName()))
              .append("\",\"phoneNumber\":\"").append(safeJson(customer.getPhoneNumber()))
              .append("\",\"email\":\"").append(safeJson(customer.getEmail()))
              .append("\",\"address\":\"").append(safeJson(customer.getAddress()))
              .append("\",\"governmentId\":\"").append(safeJson(customer.getGovernmentId()))
              .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String reservationsToJson(List<Reservation> reservations) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"reservationId\":").append(reservation.getReservationId())
              .append(",\"customerId\":").append(reservation.getCustomerId())
              .append(",\"roomNumber\":").append(reservation.getRoomNumber())
              .append(",\"checkInDate\":\"").append(reservation.getCheckInDate())
              .append("\",\"checkOutDate\":\"").append(reservation.getCheckOutDate())
              .append("\",\"numberOfGuests\":").append(reservation.getNumberOfGuests())
              .append(",\"status\":\"").append(reservation.getReservationStatus())
              .append("\",\"totalAmount\":").append(String.format("%.2f", reservation.getTotalAmount()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String paymentsToJson(List<Payment> payments) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < payments.size(); i++) {
            Payment payment = payments.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"paymentId\":").append(payment.getPaymentId())
              .append(",\"reservationId\":").append(payment.getReservationId())
              .append(",\"amount\":").append(String.format("%.2f", payment.getAmount()))
              .append(",\"paymentDate\":\"").append(payment.getPaymentDate())
              .append("\",\"paymentMethod\":\"").append(payment.getPaymentMethod())
              .append("\",\"paymentStatus\":\"").append(payment.getPaymentStatus())
              .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String getContentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css")) return "text/css; charset=UTF-8";
        if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (name.endsWith(".json")) return "application/json; charset=UTF-8";
        return "application/octet-stream";
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        sendBytes(exchange, statusCode, json.getBytes(StandardCharsets.UTF_8), "application/json; charset=UTF-8");
    }

    private static void sendText(HttpExchange exchange, int statusCode, String text, String contentType) throws IOException {
        sendBytes(exchange, statusCode, text.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private static void sendBytes(HttpExchange exchange, int statusCode, byte[] bytes, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String safeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
