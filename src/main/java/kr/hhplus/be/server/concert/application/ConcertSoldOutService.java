package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.application.exception.NotValidConcertException;
import kr.hhplus.be.server.seat.application.dto.SeatCountQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryRepository;
import org.springframework.stereotype.Service;

/**
 * 콘서트 매진 관리 서비스
 */
@Service
public class ConcertSoldOutService {

    private final SeatQueryRepository seatQueryRepository;

    public ConcertSoldOutService(SeatQueryRepository seatQueryRepository) {
        this.seatQueryRepository = seatQueryRepository;
    }

    /**
     * 매진 여부 확인
     * - 전체 좌석수와 완료 좌석수가 같으면 매진으로 판단
     * @param concertId 콘서트 식별자
     * @return boolean 매진 여부
     */
    public boolean isSoldOut(Long concertId) {
        SeatCountQueryResult seatCountQueryResult = seatQueryRepository.countSeatsByConcertId(
            concertId);

        Long totalCount = seatCountQueryResult.getTotalCount();
        Long completedCount = seatCountQueryResult.getCompletedCount();

        if (totalCount == 0) {
            throw new NotValidConcertException(concertId);
        }

        return totalCount.equals(completedCount);
    }

}
