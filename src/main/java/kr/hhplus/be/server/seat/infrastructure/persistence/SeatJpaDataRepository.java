package kr.hhplus.be.server.seat.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SeatJpaDataRepository extends JpaRepository<Seat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Seat> findWithLockById(Long id);
}
