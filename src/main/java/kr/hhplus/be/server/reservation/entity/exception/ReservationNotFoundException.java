package kr.hhplus.be.server.reservation.entity.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long reservationId) {
        super("Reservation not found for id: " + reservationId);
    }
}
