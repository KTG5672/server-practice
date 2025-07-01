package kr.hhplus.be.server.seat.infrastructure.persistence;

import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatCountQueryResult;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SeatQueryJpaRepository implements SeatQueryRepository {

    private final JpaSeatQueryRepository jpaSeatQueryRepository;

    public SeatQueryJpaRepository(JpaSeatQueryRepository jpaSeatQueryRepository) {
        this.jpaSeatQueryRepository = jpaSeatQueryRepository;
    }

    @Override
    public List<SeatQueryResult> findSeatsWithAvailability(Long scheduleId) {
        return jpaSeatQueryRepository.findSeatsWithAvailability(scheduleId);
    }

    @Override
    public SeatCountQueryResult countSeatsByConcertId(Long concertId) {
        return jpaSeatQueryRepository.findSeatsCountByConcertId(concertId);
    }
}
