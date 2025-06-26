package kr.hhplus.be.server.integration.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import kr.hhplus.be.server.payment.entity.exception.ProcessPaymentFailException;
import kr.hhplus.be.server.payment.interfaces.gateway.PaymentJpaDataRepository;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentCommand;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentUseCase;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/sql/mysql/payment-concurrency-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
public class PaymentConcurrencyTest {

    @Autowired
    ProcessPaymentUseCase processPaymentUseCase;

    @Autowired
    ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    PaymentJpaDataRepository paymentJpaDataRepository;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 동시 여러 결제 요청시 하나의 결제 요청 처리 되는지 검증한다.
     * - 분산락을 통한 동시성 제어
     */
    @Test
    @DisplayName("동시에 여러 결제 요청 시 하나의 결제만 처리된다.")
    void 동시에_여러_결제_요청시_하나의_결제만_처리된다() throws Exception {
        // given
        String userId = "payment-test-user-1";
        Long seatId = 9001L;

        Long reservationId = reserveSeatUseCase.reserveSeat(new ReserveSeatCommand(userId, seatId));

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    processPaymentUseCase.processPayment(new ProcessPaymentCommand(userId, reservationId));
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        int successResult = successCount.get();
        assertThat(successResult).isEqualTo(1);
        int failResult = failCount.get();
        assertThat(failResult).isEqualTo(threadCount - 1);
        List<Payment> payments = paymentJpaDataRepository.findAll().stream()
                .filter((payment) -> userId.equals(payment.getUserId()))
                .toList();
        assertThat(payments.size()).isEqualTo(1);
    }


    /**
     * 트랜잭션이 커밋된 다음 분산락이 제대로 해제 되는지 검증한다.
     */
    @Test
    @DisplayName("트랜잭션 커밋 이후 분산락이 해제된다.")
    void 트랜잭션_커밋_이후_분산락이_해제된다() {
        // given
        String userId = "payment-test-user-2";
        ReserveSeatCommand command = new ReserveSeatCommand(userId, 9002L);
        Long reservationId = reserveSeatUseCase.reserveSeat(command);

        // when
        processPaymentUseCase.processPayment(new ProcessPaymentCommand(userId, reservationId));

        // then
        RLock lock = redissonClient.getLock("lock:payment:user:payment-test-user-2");
        assertThat(lock.isLocked()).isFalse();
    }

    /**
     * 결제 실패 시 분산락이 제대로 해제되고 결제 상태가 실패가 되는지 검증한다.
     * - ProcessPaymentFailException 발생 시킨 후 락 해제 및 상태 검증
     */
    @Test
    @DisplayName("결제 실패 시 분산락이 해제되고 결제 상태가 실패가 된다.")
    void 결제_실패시_분산락이_해제되고_결제_상태가_실패가_된다() {
        // given
        String userId = "payment-test-user-3";
        ReserveSeatCommand command = new ReserveSeatCommand(userId, 9003L);
        Long reservationId = reserveSeatUseCase.reserveSeat(command);
        // when
        var throwableAssert = assertThatThrownBy(
            () -> processPaymentUseCase.processPayment(new ProcessPaymentCommand(userId, reservationId))
        );

        // then
        throwableAssert.isInstanceOf(ProcessPaymentFailException.class);
        RLock lock = redissonClient.getLock("lock:payment:user:payment-test-user-3");
        assertThat(lock.isLocked()).isFalse();

        List<Payment> payments = paymentJpaDataRepository.findAll().stream()
            .filter((payment) -> userId.equals(payment.getUserId()))
            .toList();
        assertThat(payments).extracting("status").containsExactly(PaymentStatus.FAIL);
    }
}
