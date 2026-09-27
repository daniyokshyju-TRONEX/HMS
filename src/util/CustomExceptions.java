class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}

class RoomNotAvailableException extends Exception {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}

class ReservationNotFoundException extends Exception {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}

class InvalidDateException extends Exception {
    public InvalidDateException(String message) {
        super(message);
    }
}
