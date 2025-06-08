package kr.hhplus.be.server.reservation.entity.exception;

public class AlreadyReservedSeatException extends RuntimeException {

    public AlreadyReservedSeatException(Long seatId) {
        super(String.format("Seat Id : %d already reserved", seatId));
    }
}
