package kr.hhplus.be.server.concert.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import kr.hhplus.be.server.concert.application.exception.NotValidConcertException;
import kr.hhplus.be.server.seat.application.dto.SeatCountQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConcertSoldOutServiceTest {
    
    @Mock
    SeatQueryRepository seatQueryRepository;
    
    ConcertSoldOutService concertSoldOutService;

    @BeforeEach
    void setUp() {
        concertSoldOutService = new ConcertSoldOutService(seatQueryRepository);
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
    
}