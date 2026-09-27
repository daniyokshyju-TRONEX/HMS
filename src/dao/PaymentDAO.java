import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    public int createPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (reservation_id, amount, payment_date, payment_method, payment_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, payment.getReservationId());
            statement.setDouble(2, payment.getAmount());
            statement.setDate(3, java.sql.Date.valueOf(payment.getPaymentDate()));
            statement.setString(4, payment.getPaymentMethod().name());
            statement.setString(5, payment.getPaymentStatus().name());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                payments.add(mapPayment(resultSet));
            }
        }
        return payments;
    }

    public List<Payment> getPaymentsByReservationId(int reservationId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE reservation_id = ? ORDER BY payment_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }
            }
        }
        return payments;
    }

    public Payment getPaymentByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE reservation_id = ? ORDER BY payment_id DESC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPayment(resultSet);
                }
            }
        }
        return null;
    }

    public void deleteByReservationId(Connection connection, int reservationId) throws SQLException {
        String sql = "DELETE FROM payments WHERE reservation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            statement.executeUpdate();
        }
    }

    private Payment mapPayment(ResultSet resultSet) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(resultSet.getInt("payment_id"));
        payment.setReservationId(resultSet.getInt("reservation_id"));
        payment.setAmount(resultSet.getDouble("amount"));
        payment.setPaymentDate(resultSet.getDate("payment_date").toLocalDate());
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(resultSet.getString("payment_method")));
        payment.setPaymentStatus(Payment.PaymentStatus.valueOf(resultSet.getString("payment_status")));
        return payment;
    }
}
