package kr.hhplus.be.server.schedule.application.query;

import java.util.List;
import kr.hhplus.be.server.schedule.application.dto.ScheduleQueryResult;

public interface ScheduleQueryRepository {

    List<ScheduleQueryResult> findAvailableSchedules(Long concertId);

}
