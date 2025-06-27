package kr.hhplus.be.server.integration.concurrency;

import static kr.hhplus.be.server.reservation.entity.ReservationStatus.CANCELLED;
import static kr.hhplus.be.server.reservation.interfaces.gateway.RedisReservationHoldManager.RESERVATION_HOLD_KEY_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import kr.hhplus.be.server.payment.interfaces.gateway.PaymentJpaDataRepository;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentCommand;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentService;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.interfaces.gateway.ReservationJpaDataRepository;
import kr.hhplus.be.server.reservation.usecase.ReservationExpirationUseCase;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatUseCase;
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
@Sql(scripts = "/sql/mysql/reservation-expiration-concurrency-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class ReservationExpirationConcurrencyTest {

    @Autowired
    ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    ReservationExpirationUseCase reservationExpirationUseCase;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    ProcessPaymentService processPaymentService;

    @Autowired
    PaymentJpaDataRepository paymentJpaDataRepository;

    @Autowired
    ReservationJpaDataRepository reservationJpaDataRepository;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    /**
     * 만료된 예약 건들에 대하여 좌석 임시 배정 해제 스케줄러와 결제가 동시에 진행될 때
     * 결제가 완료된 것이 취소 처리가 안되는지 검증한다.
     */
    @Test
    @DisplayName("좌석 임시 배정 해제 스케줄러와 결제가 동시에 실행될 때 결제가 완료된 것들이 취소처리 되면 안된다")
    void 좌석_임시_배정_해제_스케줄러와_결제가_동시에_실행될때_결제가_완료된것이_취소처리_되면_안된다() throws Exception {
        // given
        String userId = "reservation-expiration-test-user-";
        List<Long> reservationIds = new ArrayList<>();

        int threadCount = 12;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> payFailedExceptions = new ArrayList<>();

        // 예약 6건 DB, Redis 임시배정 저장
        for (int i = 0; i < 6; i++) {
            Long reservationId = reserveSeatUseCase.reserveSeat(new ReserveSeatCommand(userId + (i + 1), (long) (i + 1001)));
            reservationIds.add(reservationId);
        }
        // when
        for (Long reservationId : reservationIds) {

            // 결제 진행
            executor.submit(() -> {
                try {
                    processPaymentService.processPayment(
                        new ProcessPaymentCommand(userId + (reservationId + 1), reservationId));
                } catch (Exception payFailedException) {
                    payFailedExceptions.add(payFailedException);
                } finally {
                    latch.countDown();
                }
            });

            // 임시배정 해제
            executor.submit(() -> {
                try {
                    reservationExpirationUseCase.expireReservation();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });

            // 강제 임시배정 만료 처리 -> 원래 5분
            executor.submit(() -> {
                try {
                    Thread.sleep(50); // 만료 처리 텀 주기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                redisTemplate.delete(RESERVATION_HOLD_KEY_PREFIX + reservationId);
            });

        }

        latch.await();
        // then
        List<Reservation> cancelledReservation = reservationJpaDataRepository.findByStatus(CANCELLED);
        List<Payment> payments = paymentJpaDataRepository.findAll();
        int payFailedCount = payFailedExceptions.size();
        int paySuccessCount = payments.stream()
            .filter((payment -> PaymentStatus.SUCCESS.equals(payment.getStatus())))
            .toList().size();

        // 총 결제 시도 횟수 = 저장된 결제 + 결제 실패 횟수
        assertThat(6).isEqualTo((payFailedCount + paySuccessCount));
        // 결제 저장된 것들 중 최종상태가 취소이면 안된다.
        assertThat(payments).map(Payment::getReservationId)
                .isNotIn(cancelledReservation.stream().map(Reservation::getId).toList());
    }
}
