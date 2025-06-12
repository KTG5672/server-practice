package kr.hhplus.be.server.seat.application.query;

import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좌석 조회 서비스 - 조회 전용 서비스
 */
@Service
@Transactional(readOnly = true)
public class SeatQueryService {

    private final SeatQueryRepository seatQueryRepository;

    public SeatQueryService(SeatQueryRepository seatQueryRepository) {
        this.seatQueryRepository = seatQueryRepository;
    }

    /**
     * 콘서트 일정 ID로 좌석 조회 쿼리 - 결과에 예약 가능 여부 포함
     *
     * @param scheduleId 콘서트 일정 ID
     * @return List<SeatQueryResult>
     */
    public List<SeatQueryResult> getSeatsWithAvailability(Long scheduleId) {
        return seatQueryRepository.findSeatsWithAvailability(scheduleId)
            .stream()
            .map((seatQueryResult -> new SeatQueryResult(seatQueryResult.getId(),
                seatQueryResult.getZone(), seatQueryResult.getNo(), seatQueryResult.getPrice(),
                true)))
            .toList();
    }

}
