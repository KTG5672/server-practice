package kr.hhplus.be.server.concert.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.concert.application.exception.NotValidConcertException;
import kr.hhplus.be.server.concert.entity.Concert;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.schedule.entity.Schedule;
import kr.hhplus.be.server.seat.application.dto.SeatCountQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConcertSoldOutServiceTest {
    
    @Mock
    SeatQueryRepository seatQueryRepository;
    @Mock
    SoldOutStateManager soldOutStateManager;
    @Mock
    SoldOutRankManager soldOutRankManager;
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    ConcertRepository concertRepository;
    
    ConcertSoldOutService concertSoldOutService;

    @BeforeEach
    void setUp() {
        concertSoldOutService = new ConcertSoldOutService(seatQueryRepository, soldOutStateManager,
            soldOutRankManager, paymentRepository, concertRepository);
    }

    /**
     * 전체 좌석과 예매완료된 좌석을 카운팅/비교하여 매진여부를 반환하는지 검증한다.
     * - 결과 true
     */
    @Test
    @DisplayName("콘서트의 전체 좌석과 완료 좌석을 카운팅하여 매진여부를 반환한다. (true)")
    void 콘서트의_전체_좌석과_완료_좌석이_같으면_카운팅하여_매진여부_true_반환한다() {
        // given
        long concertId = 1L;
        SeatCountQueryResultImpl soldExpected = new SeatCountQueryResultImpl(10L, 10L);

        when(seatQueryRepository.countSeatsByConcertId(concertId)).thenReturn(soldExpected);

        // when
        boolean soldOut = concertSoldOutService.isSoldOut(concertId);

        // then
        assertThat(soldOut).isTrue();
    }
    /**
     * 전체 좌석과 예매완료된 좌석을 카운팅/비교하여 매진여부를 반환하는지 검증한다.
     * - 결과 false
     */
    @Test
    @DisplayName("콘서트의 전체 좌석과 완료 좌석을 카운팅하여 매진여부를 반환한다. (false)")
    void 콘서트의_전체_좌석과_완료_좌석이_같으면_카운팅하여_매진여부_false_반환한다_f() {
        // given
        long concertId = 1L;
        SeatCountQueryResultImpl notSoldExpected = new SeatCountQueryResultImpl(10L, 5L);
        when(seatQueryRepository.countSeatsByConcertId(concertId)).thenReturn(notSoldExpected);

        // when
        boolean soldOut = concertSoldOutService.isSoldOut(concertId);

        // then
        assertThat(soldOut).isFalse();
    }

    /**
     * 콘서트의 전체좌석이 0개일 경우 유효하지 않으므로 NotValidConcertException 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("콘서트의 전체 좌석수가 0이면 예외를 발생시킨다.")
    void 콘서트의_전체_좌석수가_0이면_예외를_발생시킨다() {
        // given
        long concertId = 1L;
        SeatCountQueryResultImpl expected = new SeatCountQueryResultImpl(0L, null);
        when(seatQueryRepository.countSeatsByConcertId(concertId)).thenReturn(expected);
        // when
        var throwableAssert = assertThatThrownBy(() -> concertSoldOutService.isSoldOut(concertId));
        // then
        throwableAssert.isInstanceOf(NotValidConcertException.class);
    }

    static class SeatCountQueryResultImpl implements SeatCountQueryResult {

        private final Long totalCount;
        private final Long completedCount;

        public SeatCountQueryResultImpl(Long totalCount, Long completedCount) {
            this.totalCount = totalCount;
            this.completedCount = completedCount;
        }

        @Override
        public Long getTotalCount() {
            return totalCount;
        }

        @Override
        public Long getCompletedCount() {
            return completedCount;
        }
    }

    /**
     * 매진 시 빠른 매진 랭킹을 기록하는지 검증한다.
     */
    @Test
    @DisplayName("매진 시 빠른 매진 랭킹을 기록한다.")
    void 매진시_빠른_매진_랭킹을_기록한다() {
        // given
        long paymentId = 1L;
        Concert testConcert = getTestConcert();
        LocalDateTime paymentAt = LocalDateTime.now();
        Long concertId = testConcert.getId();
        LocalDateTime earliestTicketOpenDateTime = testConcert.getEarliestTicketOpenDateTime();

        when(paymentRepository.findById(any())).thenReturn(Optional.of(new Payment(paymentId, null, null, 1000, null,
            paymentAt)));
        when(concertRepository.findById(any())).thenReturn(Optional.of(testConcert));
        when(soldOutStateManager.addIfAbsent(concertId, paymentAt)).thenReturn(Boolean.TRUE);
        // when
        concertSoldOutService.soldOut(concertId, paymentId);

        // then
        long seconds = Duration.between(earliestTicketOpenDateTime, paymentAt).get(ChronoUnit.SECONDS);
        verify(soldOutRankManager).recordSoldOut(concertId, seconds);
    }


    /**
     * 빠른 매진 랭킹은 초기 매진만 기록하므로 중복 기록 하지 않는것을 검증한다.
     */
    @Test
    @DisplayName("초기 매진이 아니면 빠른 매진 랭킹을 기록하지 않는다.")
    void 초기_매진이_아니면_빠른_매진_랭킹_기록을_하지_않는다() {
        // given
        long paymentId = 1L;
        Concert testConcert = getTestConcert();
        LocalDateTime paymentAt = LocalDateTime.now();
        Long concertId = testConcert.getId();
        LocalDateTime earliestTicketOpenDateTime = testConcert.getEarliestTicketOpenDateTime();

        when(paymentRepository.findById(any())).thenReturn(Optional.of(new Payment(paymentId, null, null, 1000, null,
            paymentAt)));
        when(concertRepository.findById(any())).thenReturn(Optional.of(testConcert));
        when(soldOutStateManager.addIfAbsent(concertId, paymentAt)).thenReturn(Boolean.FALSE);
        // when
        concertSoldOutService.soldOut(concertId, paymentId);

        // then
        long seconds = Duration.between(earliestTicketOpenDateTime, paymentAt).get(ChronoUnit.SECONDS);
        verify(soldOutRankManager, never()).recordSoldOut(concertId, seconds);
    }

    private Concert getTestConcert() {
        Concert concert = null;
        try {
            Class<Concert> concertClass = Concert.class;
            Constructor<Concert> constructor = concertClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            concert = constructor.newInstance();
            ReflectionTestUtils.setField(concert, "id", 1L);
            ReflectionTestUtils.setField(concert, "schedules", List.of(getTestSchedule()));
        } catch (Exception ignored) {
            fail();
        }
        return concert;
    }

    private Schedule getTestSchedule() {
        Schedule schedule = null;
        try {
            Class<Schedule> scheduleClass = Schedule.class;
            Constructor<Schedule> constructor = scheduleClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            schedule = constructor.newInstance();
            ReflectionTestUtils.setField(schedule, "id", 2L);
            ReflectionTestUtils.setField(schedule, "ticketOpenDateTime", getTestTicketOpenDateTime());
        } catch (Exception ignored) {
            fail();
        }
        return schedule;
    }

    private LocalDateTime getTestTicketOpenDateTime() {
        return LocalDateTime.of(2025, 7, 1, 10, 30);
    }

    
}