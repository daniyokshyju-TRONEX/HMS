import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public void addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, availability_status) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, room.getRoomNumber());
            statement.setString(2, room.getRoomType().name());
            statement.setDouble(3, room.getPricePerNight());
            statement.setString(4, room.getAvailabilityStatus().name());
            statement.executeUpdate();
        }
    }

    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rooms.add(mapRoom(resultSet));
            }
        }
        return rooms;
    }

    public Room getRoomByNumber(int roomNumber) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRoom(resultSet);
                }
            }
        }
        return null;
    }

    public List<Room> getAvailableRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE availability_status = 'AVAILABLE' ORDER BY room_number";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rooms.add(mapRoom(resultSet));
            }
        }
        return rooms;
    }

    public void updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_type = ?, price_per_night = ?, availability_status = ? WHERE room_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, room.getRoomType().name());
            statement.setDouble(2, room.getPricePerNight());
            statement.setString(3, room.getAvailabilityStatus().name());
            statement.setInt(4, room.getRoomNumber());
            statement.executeUpdate();
        }
    }

    public void updateAvailability(int roomNumber, Room.AvailabilityStatus status) throws SQLException {
        String sql = "UPDATE rooms SET availability_status = ? WHERE room_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, roomNumber);
            statement.executeUpdate();
        }
    }

    public void updateAvailability(Connection connection, int roomNumber, Room.AvailabilityStatus status) throws SQLException {
        String sql = "UPDATE rooms SET availability_status = ? WHERE room_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, roomNumber);
            statement.executeUpdate();
        }
    }

    public void deleteRoom(int roomNumber) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomNumber);
            statement.executeUpdate();
        }
    }

    private Room mapRoom(ResultSet resultSet) throws SQLException {
        Room room = new Room();
        room.setRoomNumber(resultSet.getInt("room_number"));
        room.setRoomType(Room.RoomType.valueOf(resultSet.getString("room_type")));
        room.setPricePerNight(resultSet.getDouble("price_per_night"));
        room.setAvailabilityStatus(Room.AvailabilityStatus.valueOf(resultSet.getString("availability_status")));
        return room;
    }
}
