import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReportService {
    public Map<String, Long> getSummary() throws SQLException {
        Map<String, Long> summary = new HashMap<>();
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM rooms) AS total_rooms, " +
                "(SELECT COUNT(*) FROM rooms WHERE availability_status = 'AVAILABLE') AS available_rooms, " +
                "(SELECT COUNT(*) FROM rooms WHERE availability_status = 'OCCUPIED') AS occupied_rooms, " +
                "(SELECT COUNT(*) FROM customers) AS total_customers, " +
                "(SELECT COUNT(*) FROM reservations WHERE reservation_status IN ('PENDING','CONFIRMED','CHECKED_IN')) AS active_reservations, " +
                "(SELECT COUNT(*) FROM reservations WHERE reservation_status = 'CHECKED_OUT') AS completed_reservations, " +
                "(SELECT COUNT(*) FROM payments) AS total_payments";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                summary.put("totalRooms", resultSet.getLong("total_rooms"));
                summary.put("availableRooms", resultSet.getLong("available_rooms"));
                summary.put("occupiedRooms", resultSet.getLong("occupied_rooms"));
                summary.put("totalCustomers", resultSet.getLong("total_customers"));
                summary.put("activeReservations", resultSet.getLong("active_reservations"));
                summary.put("completedReservations", resultSet.getLong("completed_reservations"));
                summary.put("totalPayments", resultSet.getLong("total_payments"));
            }
        }
        return summary;
    }
}
