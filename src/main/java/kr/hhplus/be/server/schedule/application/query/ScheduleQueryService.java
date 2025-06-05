package kr.hhplus.be.server.schedule.application.query;

import java.util.List;
import kr.hhplus.be.server.schedule.application.dto.ScheduleQueryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ScheduleQueryService {

    private final ScheduleQueryRepository scheduleQueryRepository;

    public ScheduleQueryService(ScheduleQueryRepository scheduleQueryRepository) {
        this.scheduleQueryRepository = scheduleQueryRepository;
    }

    public List<ScheduleQueryResult> getAvailableSchedules(Long concertId) {
        return scheduleQueryRepository.findAvailableSchedules(concertId);
    }

}
