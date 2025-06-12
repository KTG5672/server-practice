package kr.hhplus.be.server.reservation.entity.exception;

import kr.hhplus.be.server.reservation.entity.ReservationStatus;

public class CannotPayReservationException extends RuntimeException {

    public CannotPayReservationException(Long id, String msg) {
        super("Cannot pay reservation: id : " + id + ", msg : " + msg);
    }

    public CannotPayReservationException(Long id, ReservationStatus status) {
        super("Cannot pay reservation: id : " + id + ", status : " + status.name());
    }
}
