import java.time.LocalDate;

public class Payment {
    public enum PaymentMethod {
        CASH, CARD, UPI
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED
    }

    private int paymentId;
    private int reservationId;
    private double amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    public Payment() {
    }

    public Payment(int paymentId, int reservationId, double amount, LocalDate paymentDate,
                   PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", reservationId=" + reservationId +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", paymentMethod=" + paymentMethod +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
