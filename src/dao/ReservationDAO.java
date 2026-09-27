import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    public int createReservation(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (customer_id, room_number, check_in_date, check_out_date, number_of_guests, reservation_status, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, reservation.getCustomerId());
            statement.setInt(2, reservation.getRoomNumber());
            statement.setDate(3, java.sql.Date.valueOf(reservation.getCheckInDate()));
            statement.setDate(4, java.sql.Date.valueOf(reservation.getCheckOutDate()));
            statement.setInt(5, reservation.getNumberOfGuests());
            statement.setString(6, reservation.getReservationStatus().name());
            statement.setDouble(7, reservation.getTotalAmount());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY reservation_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reservations.add(mapReservation(resultSet));
            }
        }
        return reservations;
    }

    public Reservation getReservationById(int reservationId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapReservation(resultSet);
                }
            }
        }
        return null;
    }

    public void cancelReservation(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = 'CANCELLED' WHERE reservation_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            statement.executeUpdate();
        }
    }

    public void deleteReservation(Connection connection, int reservationId) throws SQLException {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            statement.executeUpdate();
        }
    }

    public void updateReservationStatus(int reservationId, Reservation.ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, reservationId);
            statement.executeUpdate();
        }
    }

    public void updateReservationStatus(Connection connection, int reservationId, Reservation.ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, reservationId);
            statement.executeUpdate();
        }
    }

    public List<Reservation> getReservationsByCustomerId(int customerId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE customer_id = ? ORDER BY reservation_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
            }
        }
        return reservations;
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(resultSet.getInt("reservation_id"));
        reservation.setCustomerId(resultSet.getInt("customer_id"));
        reservation.setRoomNumber(resultSet.getInt("room_number"));
        reservation.setCheckInDate(resultSet.getDate("check_in_date").toLocalDate());
        reservation.setCheckOutDate(resultSet.getDate("check_out_date").toLocalDate());
        reservation.setNumberOfGuests(resultSet.getInt("number_of_guests"));
        reservation.setReservationStatus(Reservation.ReservationStatus.valueOf(resultSet.getString("reservation_status")));
        reservation.setTotalAmount(resultSet.getDouble("total_amount"));
        return reservation;
    }
}
