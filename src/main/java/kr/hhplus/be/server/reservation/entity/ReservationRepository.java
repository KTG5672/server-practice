package kr.hhplus.be.server.reservation.entity;

import java.util.List;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    List<Reservation> findBySeatId(Long seatId);
}
