package kr.hhplus.be.server.seat.entity;

import java.util.Optional;

public interface SeatRepository {
    Optional<Seat> findById(Long id);
    Optional<Seat> findWithLockById(Long id);
}
