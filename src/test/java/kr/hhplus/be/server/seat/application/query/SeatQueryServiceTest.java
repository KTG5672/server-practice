package kr.hhplus.be.server.seat.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatQueryServiceTest {
    
    @Mock
    SeatQueryRepository seatQueryRepository;
    
    SeatQueryService seatQueryService;

    @BeforeEach
    void setUp() {
        seatQueryService = new SeatQueryService(seatQueryRepository);
    }

    /**
     * 콘서트 일정 ID로 좌석 정보 및 예약 가능여부를 정상 조회하는지 검증한다.
     */
    @Test
    @DisplayName("콘서트 일정 식별자로 좌석 정보 및 예약 가능여부를 조회한다")
    void 콘서트_일정_식별자로_좌석_정보_및_예약_가능여부_를_조회한다() {
        // given
        Long scheduleId = 1L;
        List<SeatQueryResult> expectedSeats = List.of(
            new SeatQueryResult(1L, "A", 1, 50_000, true)
            , new SeatQueryResult(2L, "B", 2, 30_000, true)
            , new SeatQueryResult(3L, "C", 3, 50_000, true));
        when(seatQueryRepository.findSeatsWithAvailability(scheduleId)).thenReturn(expectedSeats);

        // when
        List<SeatQueryResult> results = seatQueryService.getSeatsWithAvailability(scheduleId);

        // then
        verify(seatQueryRepository, times(1)).findSeatsWithAvailability(scheduleId);
        assertThat(results).hasSize(3);
        assertThat(results).extracting("zone").containsExactly("A", "B", "C");
        assertThat(results).extracting("no").containsExactly(1, 2, 3);
        assertThat(results).extracting("available").containsExactly(true, true, true);
    }

    /**
     * 저장소에서 빈 좌석 리스트를 조회해도 빈 값으로 정상적으로 반환하는지 검증한다.
     */
    @Test
    @DisplayName("조회된 좌석이 없어도 빈 값으로 정상적으로 반환한다")
    void 조회된_좌석이_없어도_빈값으로_정상적으로_반환한다() {
        // given
        Long scheduleId = 1L;
        when(seatQueryRepository.findSeatsWithAvailability(scheduleId)).thenReturn(Collections.emptyList());

        // when
        List<SeatQueryResult> results = seatQueryService.getSeatsWithAvailability(scheduleId);

        // then
        assertThat(results).isEmpty();
    }
}