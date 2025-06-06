package kr.hhplus.be.server.schedule.infrastructure.persistence;

import java.util.List;
import kr.hhplus.be.server.schedule.application.dto.ScheduleQueryResult;
import kr.hhplus.be.server.schedule.application.query.ScheduleQueryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ScheduleQueryJpaRepository implements ScheduleQueryRepository {

    @Override
    public List<ScheduleQueryResult> findAvailableSchedules(Long concertId) {
        return List.of();
    }
}
