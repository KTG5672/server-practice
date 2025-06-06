package kr.hhplus.be.server.seat.entity.exception;

public class SeatNotFoundException extends RuntimeException {

    public SeatNotFoundException(Long seatId) {
        super("Seat not found with id: " + seatId);
    }
}
