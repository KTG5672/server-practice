package kr.hhplus.be.server.concert.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.concert.application.exception.ConcertNotFoundException;
import kr.hhplus.be.server.concert.application.exception.NotValidConcertException;
import kr.hhplus.be.server.concert.entity.Concert;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.payment.entity.exception.PaymentNotFoundException;
import kr.hhplus.be.server.seat.application.dto.SeatCountQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 콘서트 매진 관리 서비스
 */
@Service
@Transactional(readOnly = true)
public class ConcertSoldOutService {

    private final SeatQueryRepository seatQueryRepository;
    private final SoldOutStateManager soldOutStateManager;
    private final SoldOutRankManager soldOutRankManager;
    private final PaymentRepository paymentRepository;
    private final ConcertRepository concertRepository;

    public ConcertSoldOutService(SeatQueryRepository seatQueryRepository,
        SoldOutStateManager soldOutStateManager, SoldOutRankManager soldOutRankManager,
        PaymentRepository paymentRepository, ConcertRepository concertRepository) {
        this.seatQueryRepository = seatQueryRepository;
        this.soldOutStateManager = soldOutStateManager;
        this.soldOutRankManager = soldOutRankManager;
        this.paymentRepository = paymentRepository;
        this.concertRepository = concertRepository;
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

    /**
     * 콘서트 매진 처리
     * - 콘서트 매진 상태 저장 (중복 저장 X)
     * - 매진 상태가 초기 저장일 때 빠른 매진 랭킹 기록
     * @param concertId 콘서트 식별자
     * @param paymentId 결제 식별자
     */
    public void soldOut(Long concertId, Long paymentId) {
        LocalDateTime paymentAt = getPaymentAtById(paymentId);
        Concert concert = getConcertById(concertId);
        LocalDateTime concertTicketOpenTime = Optional.of(concert.getEarliestTicketOpenDateTime())
            .orElseThrow(() -> new NotValidConcertException(concertId));

        long seconds = Duration.between(concertTicketOpenTime, paymentAt).getSeconds();

        if (soldOutStateManager.addIfAbsent(concertId, paymentAt)) {
            soldOutRankManager.recordSoldOut(concertId, seconds);
        }
    }

    private LocalDateTime getPaymentAtById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return payment.getPaymentAt();
    }

    private Concert getConcertById(Long concertId) {
        return concertRepository.findById(concertId)
            .orElseThrow(() -> new ConcertNotFoundException(concertId));
    }

}
