package kr.hhplus.be.server.schedule.application.query;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import kr.hhplus.be.server.schedule.application.dto.ScheduleQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleQueryServiceTest {

    @Mock
    ScheduleQueryRepository scheduleQueryRepository;

    ScheduleQueryService scheduleQueryService;

    @BeforeEach
    void setUp() {
        scheduleQueryService = new ScheduleQueryService(scheduleQueryRepository);
    }

    /**
     * 콘서트 ID를 받아 예약 가능한 콘서트 일정을 정상 조회하는지 검증한다.
     */
    @Test
    @DisplayName("콘서트 식별자로 예약 가능한 콘서트 일정들을 조회한다.")
    void 콘서트_식별자로_예약_가능한_콘서트_일정을_조회한다() {
        // given
        Long concertId = 1L;
        LocalDate availableDay = LocalDate.now().plusDays(10);
        LocalDateTime ticketOpenDateTime = LocalDateTime.now();
        List<ScheduleQueryResult> expectedSchedules = List.of(
            new ScheduleQueryResult(1L, "Concert-A", "Place-A", LocalDateTime.of(availableDay,
                LocalTime.of(8, 0)), ticketOpenDateTime)
            , new ScheduleQueryResult(2L, "Concert-B", "Place-B", LocalDateTime.of(availableDay,
                LocalTime.of(10, 0)), ticketOpenDateTime)
            , new ScheduleQueryResult(3L, "Concert-C", "Place-C", LocalDateTime.of(availableDay,
                LocalTime.of(12, 0)), ticketOpenDateTime)
        );
        when(scheduleQueryRepository.findAvailableSchedules(concertId)).thenReturn(expectedSchedules);

        // when
        List<ScheduleQueryResult> results = scheduleQueryService.getAvailableSchedules(
            concertId);

        // then
        verify(scheduleQueryRepository, times(1)).findAvailableSchedules(concertId);
        assertThat(results).hasSize(3);
        assertThat(results).extracting("concertName").containsExactly("Concert-A", "Concert-B", "Concert-C");
        assertThat(results).extracting("place").containsExactly("Place-A", "Place-B", "Place-C");
        assertThat(results).extracting("ticketOpenDateTime").containsExactly(ticketOpenDateTime, ticketOpenDateTime, ticketOpenDateTime);
        assertThat(results).extracting("startDateTime").containsExactly(
            LocalDateTime.of(availableDay, LocalTime.of(8, 0))
                , LocalDateTime.of(availableDay, LocalTime.of(10, 0))
                , LocalDateTime.of(availableDay, LocalTime.of(12, 0)));
    }

    /**
     * 저장소에서 빈 일정 리스트를 조회해도 빈 값으로 정상적으로 반환하는지 검증한다.
     */
    @Test
    @DisplayName("조회된 일정이 없어도 빈 값으로 정상적으로 반환한다")
    void 조회된_일정이_없어도_빈값으로_정상적으로_반환한다() {
        // given
        Long concertId = 1L;
        when(scheduleQueryRepository.findAvailableSchedules(concertId)).thenReturn(Collections.emptyList());

        // when
        List<ScheduleQueryResult> results = scheduleQueryService.getAvailableSchedules(concertId);

        // then
        assertThat(results).isEmpty();
    }
}