import java.sql.SQLException;
import java.time.LocalDate;

public class PaymentService {
    private final PaymentDAO paymentDAO;
    private final ReservationDAO reservationDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public Payment createPayment(int reservationId, double amount, String method) throws SQLException, ReservationNotFoundException {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            throw new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
        }

        if (!InputValidator.isPositiveNumber(amount)) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        Payment.PaymentMethod paymentMethod;
        try {
            paymentMethod = Payment.PaymentMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method. Use CASH, CARD, or UPI.");
        }

        Payment payment = new Payment(
                0,
                reservationId,
                amount,
                LocalDate.now(),
                paymentMethod,
                Payment.PaymentStatus.PAID
        );

        int paymentId = paymentDAO.createPayment(payment);
        payment.setPaymentId(paymentId);
        return payment;
    }

    public Payment getPaymentByReservationId(int reservationId) throws SQLException {
        return paymentDAO.getPaymentByReservationId(reservationId);
    }

    public java.util.List<Payment> getAllPayments() throws SQLException {
        return paymentDAO.getAllPayments();
    }
}
