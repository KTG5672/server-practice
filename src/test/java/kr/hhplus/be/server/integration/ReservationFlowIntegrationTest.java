package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenManager;
import kr.hhplus.be.server.common.queue.application.QueueTokenService;
import kr.hhplus.be.server.common.queue.application.QueueTokenStatus;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentCommand;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentUseCase;
import kr.hhplus.be.server.reservation.usecase.ReservationExpirationUseCase;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatUseCase;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/sql/mysql/reservation-flow-integration-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class ReservationFlowIntegrationTest {

    @Autowired
    QueueTokenService queueTokenService;
    @Autowired
    SeatQueryService seatQueryService;
    @Autowired
    ReserveSeatUseCase reserveSeatUseCase;
    @Autowired
    ProcessPaymentUseCase processPaymentUseCase;
    @Autowired
    ReservationExpirationUseCase reservationExpirationUseCase;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    QueueTokenManager queueTokenManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    @DisplayName("토큰발급/예약요청/결제처리 전체 흐름 테스트")
    void 토큰_발급_예약_요청_결제_처리_전체_흐름_테스트() {
        // given
        String userId = "test-user";

        // 1. 대기열 토큰 발급
        QueueTokenInfo tokenInfo = queueTokenService.enterQueueAndGetToken(userId);
        assertThat(tokenInfo.token()).isNotNull();
        queueTokenManager.moveWaitQueueToActiveQueue();
        QueueTokenInfo token = queueTokenService.getToken(tokenInfo.token());

        assertThat(token.status()).isEqualTo(QueueTokenStatus.ACTIVE);

        // 2. 예약 가능한 좌석 조회
        Long scheduleId = 2L;
        List<SeatQueryResult> seats = seatQueryService.getSeatsWithAvailability(scheduleId);
        SeatQueryResult seat = seats.get(0);

        // 3. 좌석 예약 (임시 배정)
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seat.getId());
        Long reservationId = reserveSeatUseCase.reserveSeat(reserveSeatCommand);
        assertThat(reservationId).isNotNull();

        // 4. 결제 요청
        ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(userId, reservationId);
        processPaymentUseCase.processPayment(paymentCommand);

    }


    @Test
    @DisplayName("임시배정된 예약이 만료된 후 다시 예약이 가능하다.")
    void 임시배정된_예약이_만료된_후_다시_예약이_가능하다() throws Exception {
        // given
        String userId = "test-user-expire";
        Long scheduleId = 2L;

        // 1. 대기열 토큰 발급
        QueueTokenInfo tokenInfo = queueTokenService.enterQueueAndGetToken(userId);
        queueTokenManager.moveWaitQueueToActiveQueue();
        QueueTokenInfo token = queueTokenService.getToken(tokenInfo.token());

        assertThat(token.status()).isEqualTo(QueueTokenStatus.ACTIVE);

        // 2. 예약 가능한 좌석 조회
        List<SeatQueryResult> seats = seatQueryService.getSeatsWithAvailability(scheduleId);
        SeatQueryResult seat = seats.get(0);

        // 3. 좌석 예약 (임시 배정)
        Long reservationId = reserveSeatUseCase.reserveSeat(
            new ReserveSeatCommand(userId, seat.getId()));
        assertThat(reservationId).isNotNull();

        // 4. TTL 설정 및 만료될 때까지 대기 (5분 TTL 가정 -> 테스트 TTL 설정 0.1초)
        redisTemplate.expire("reservation:hold:" + reservationId, Duration.ofMillis(100));
        Thread.sleep(500); // 0.5초 대기
        assertThat(redisTemplate.hasKey("reservation:hold:" + reservationId)).isFalse();

        // 5. 만료된 예약 취소 처리 (스케줄러 -> 테스트 직접 실행)
        reservationExpirationUseCase.expireReservation();

        // 6. 예약 가능한 좌석 재조회
        List<SeatQueryResult> updatedSeats = seatQueryService.getSeatsWithAvailability(scheduleId);
        boolean seatIsAvailable = updatedSeats.stream()
            .anyMatch(s -> s.getId().equals(seat.getId()) && s.isAvailable());

        assertThat(seatIsAvailable).isTrue();
    }

}

