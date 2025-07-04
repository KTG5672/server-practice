package kr.hhplus.be.server.concert.application.processor;

import kr.hhplus.be.server.concert.application.ConcertSoldOutService;
import kr.hhplus.be.server.concert.entity.Concert;
import kr.hhplus.be.server.schedule.entity.Schedule;
import kr.hhplus.be.server.seat.entity.Seat;
import kr.hhplus.be.server.seat.entity.SeatRepository;
import kr.hhplus.be.server.seat.entity.exception.SeatNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매진 프로세스를 담당하는 프로세서
 */
@Component
public class ConcertSoldOutProcessor {

    private final ConcertSoldOutService concertSoldOutService;
    private final SeatRepository seatRepository;

    public ConcertSoldOutProcessor(ConcertSoldOutService concertSoldOutService,
        SeatRepository seatRepository) {
        this.concertSoldOutService = concertSoldOutService;
        this.seatRepository = seatRepository;
    }

    /**
     * 실행 메서드
     * - 매진 상태를 체크하고 매진이면 매진 처리 서비스 호출
     * @param seatId 좌석 식별자
     * @param paymentId 결제 식별자
     */
    @Transactional(readOnly = true)
    public void process(Long seatId, Long paymentId) {

        Concert concert = getConcertBySeatId(seatId);
        Long concertId = concert.getId();

        boolean isSoldOut = concertSoldOutService.isSoldOut(concertId);
        if (!isSoldOut) {
            return;
        }
        concertSoldOutService.soldOut(concertId, paymentId);
    }

    private Concert getConcertBySeatId(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatNotFoundException(seatId));
        Schedule schedule = seat.getSchedule();
        return schedule.getConcert();
    }
}
