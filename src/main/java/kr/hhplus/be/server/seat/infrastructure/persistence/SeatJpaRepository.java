package kr.hhplus.be.server.seat.infrastructure.persistence;

import java.util.Optional;
import kr.hhplus.be.server.seat.entity.Seat;
import kr.hhplus.be.server.seat.entity.SeatRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SeatJpaRepository implements SeatRepository {

    @Override
    public Optional<Seat> findById(Long id) {
        return Optional.empty();
    }
}
