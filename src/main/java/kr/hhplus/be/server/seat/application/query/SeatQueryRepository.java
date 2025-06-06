package kr.hhplus.be.server.seat.application.query;

import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;

public interface SeatQueryRepository {

    List<SeatQueryResult> findSeatsWithAvailability(Long scheduleId);

}
