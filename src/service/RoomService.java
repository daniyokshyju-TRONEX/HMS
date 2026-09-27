import java.sql.SQLException;
import java.util.List;

public class RoomService {
    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    public void addRoom(Room room) throws SQLException {
        roomDAO.addRoom(room);
    }

    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.getAllRooms();
    }

    public Room getRoomByNumber(int roomNumber) throws SQLException {
        return roomDAO.getRoomByNumber(roomNumber);
    }

    public List<Room> getAvailableRooms() throws SQLException {
        return roomDAO.getAvailableRooms();
    }

    public void updateRoom(Room room) throws SQLException {
        roomDAO.updateRoom(room);
    }

    public void deleteRoom(int roomNumber) throws SQLException {
        roomDAO.deleteRoom(roomNumber);
    }
}
