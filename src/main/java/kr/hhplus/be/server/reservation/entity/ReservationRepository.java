package kr.hhplus.be.server.reservation.entity;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    List<Reservation> findBySeatId(Long seatId);
    Optional<Reservation> findById(Long id);
    List<Reservation> findByStatus(ReservationStatus status);
    int updateReservationStatusByIdAndStatus(Long id, ReservationStatus status, ReservationStatus newStatus);
}
