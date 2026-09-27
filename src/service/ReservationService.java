import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final CustomerDAO customerDAO;
    private final RoomDAO roomDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.customerDAO = new CustomerDAO();
        this.roomDAO = new RoomDAO();
    }

    public Reservation createReservation(int customerId, int roomNumber, LocalDate checkIn, LocalDate checkOut,
                                        int numberOfGuests)
            throws SQLException, CustomerNotFoundException, RoomNotAvailableException, InvalidDateException {

        if (!InputValidator.isValidCustomerId(customerId)) {
            throw new CustomerNotFoundException("Invalid customer ID.");
        }

        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        Room room = roomDAO.getRoomByNumber(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room number " + roomNumber + " does not exist.");
        }

        if (!room.isAvailable()) {
            throw new RoomNotAvailableException("Room " + roomNumber + " is currently not available.");
        }

        if (!InputValidator.isValidDateRange(checkIn, checkOut)) {
            throw new InvalidDateException("Check-out date must be after the check-in date.");
        }

        if (!InputValidator.isPositiveNumber(numberOfGuests)) {
            throw new IllegalArgumentException("Number of guests must be greater than zero.");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalAmount = nights * room.getPricePerNight();

        Reservation reservation = new Reservation(
                0,
                customerId,
                roomNumber,
                checkIn,
                checkOut,
                numberOfGuests,
                Reservation.ReservationStatus.PENDING,
                totalAmount
        );

        int reservationId = reservationDAO.createReservation(reservation);
        reservation.setReservationId(reservationId);
        return reservation;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return reservationDAO.getAllReservations();
    }

    public Reservation getReservationById(int reservationId) throws SQLException {
        return reservationDAO.getReservationById(reservationId);
    }

    public void cancelReservation(int reservationId) throws SQLException {
        reservationDAO.cancelReservation(reservationId);
    }

    public Reservation checkInReservation(int reservationId) throws SQLException, ReservationNotFoundException {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            throw new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
        }

        if (reservation.getReservationStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is cancelled and cannot be checked in.");
        }

        if (reservation.getReservationStatus() == Reservation.ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Reservation is already checked in.");
        }

        Customer customer = customerDAO.getCustomerById(reservation.getCustomerId());
        if (customer == null) {
            throw new IllegalStateException("Customer does not exist for this reservation.");
        }

        Room room = roomDAO.getRoomByNumber(reservation.getRoomNumber());
        if (room == null) {
            throw new IllegalStateException("Room does not exist for this reservation.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                reservationDAO.updateReservationStatus(connection, reservationId, Reservation.ReservationStatus.CHECKED_IN);
                roomDAO.updateAvailability(connection, reservation.getRoomNumber(), Room.AvailabilityStatus.OCCUPIED);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }

        return reservationDAO.getReservationById(reservationId);
    }

    public Reservation checkOutReservation(int reservationId) throws SQLException, ReservationNotFoundException {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            throw new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
        }

        if (reservation.getReservationStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Reservation is not active. It must be checked in before checkout.");
        }

        Room room = roomDAO.getRoomByNumber(reservation.getRoomNumber());
        if (room == null) {
            throw new IllegalStateException("Room does not exist for this reservation.");
        }

        double finalBill = reservation.getNumberOfNights() * room.getPricePerNight();
        reservation.setTotalAmount(finalBill);

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                reservationDAO.updateReservationStatus(connection, reservationId, Reservation.ReservationStatus.CHECKED_OUT);
                roomDAO.updateAvailability(connection, reservation.getRoomNumber(), Room.AvailabilityStatus.AVAILABLE);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }

        return reservationDAO.getReservationById(reservationId);
    }

    public double calculateBill(int reservationId) throws SQLException {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            return 0;
        }
        Room room = roomDAO.getRoomByNumber(reservation.getRoomNumber());
        if (room == null) {
            return 0;
        }
        return reservation.getNumberOfNights() * room.getPricePerNight();
    }
}
