import java.util.Objects;

public class Room {
    public enum RoomType {
        SINGLE, DOUBLE, DELUXE, SUITE
    }

    public enum AvailabilityStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }

    private int roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private AvailabilityStatus availabilityStatus;

    public Room() {
    }

    public Room(int roomNumber, RoomType roomType, double pricePerNight, AvailabilityStatus availabilityStatus) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.availabilityStatus = availabilityStatus;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public boolean isAvailable() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomType=" + roomType +
                ", pricePerNight=" + pricePerNight +
                ", availabilityStatus=" + availabilityStatus +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Room room = (Room) obj;
        return roomNumber == room.roomNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }
}
