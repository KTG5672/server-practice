package kr.hhplus.be.server.seat.infrastructure.persistence;

import java.util.Optional;
import kr.hhplus.be.server.seat.entity.Seat;
import kr.hhplus.be.server.seat.entity.SeatRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SeatJpaRepository implements SeatRepository {

    private final SeatJpaDataRepository seatJpaDataRepository;

    public SeatJpaRepository(SeatJpaDataRepository seatJpaDataRepository) {
        this.seatJpaDataRepository = seatJpaDataRepository;
    }

    @Override
    public Optional<Seat> findById(Long id) {
        return seatJpaDataRepository.findById(id);
    }

    @Override
    public Optional<Seat> findWithLockById(Long id) {
        return seatJpaDataRepository.findWithLockById(id);
    }
}
