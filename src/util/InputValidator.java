import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class InputValidator {
    private InputValidator() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isPositiveNumber(int value) {
        return value > 0;
    }

    public static boolean isPositiveNumber(double value) {
        return value > 0;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9+()\\-\\s]{10,20}$");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidRoomNumber(int roomNumber) {
        return roomNumber > 0;
    }

    public static boolean isValidCustomerId(int customerId) {
        return customerId > 0;
    }

    public static LocalDate parseDate(String input) throws InvalidDateException {
        if (isBlank(input)) {
            throw new InvalidDateException("Date cannot be empty.");
        }

        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Invalid date format. Use YYYY-MM-DD");
        }
    }

    public static boolean isValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        return checkIn != null && checkOut != null && checkOut.isAfter(checkIn);
    }
}
