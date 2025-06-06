package kr.hhplus.be.server.seat.infrastructure.persistence;

import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SeatQueryJpaRepository implements SeatQueryRepository {

    @Override
    public List<SeatQueryResult> findSeatsWithAvailability(Long scheduleId) {
        return List.of();
    }
}
