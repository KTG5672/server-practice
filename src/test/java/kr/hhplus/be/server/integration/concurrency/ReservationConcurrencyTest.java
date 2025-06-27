package kr.hhplus.be.server.integration.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.interfaces.gateway.ReservationJpaDataRepository;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatUseCase;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import kr.hhplus.be.server.seat.application.query.SeatQueryService;
import kr.hhplus.be.server.seat.entity.exception.SeatNotFoundException;
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
@Sql(scripts = "/sql/mysql/reservation-concurrency-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class ReservationConcurrencyTest {

    @Autowired
    SeatQueryService seatQueryService;

    @Autowired
    ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    ReservationJpaDataRepository reservationJpaDataRepository;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 1개의 좌석에 여러명의 동시 예약 요청 시 1명만 성공하는지 검증한다.
     * n (유저) - 1 (좌석)
     * 분산락으로 변경 후 재검증
     * @throws Exception Exceptions
     */
    @Test
    @DisplayName("동시에 여러명이 하나의 좌석 예약 요청 시 한 명만 성공한다.")
    void 동시에_여러명이_하나의_좌석_예약_요청시_한명만_성공한다() throws Exception {
        // given
        Long scheduleId = 2001L;
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<SeatQueryResult> seats = seatQueryService.getSeatsWithAvailability(scheduleId);
        SeatQueryResult seat = seats.get(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            String userId = "test-user-" + i;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    latch.await();
                    try {
                        reserveSeatUseCase.reserveSeat(new ReserveSeatCommand(userId, seat.getId()));
                        successCount.incrementAndGet();
                    } catch (Exception ignored) {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executor));
        }

        latch.countDown();
        for (CompletableFuture<Void> future : futures) {
            future.get();
        }

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }

    /**
     * 여러개의 좌석에 여러명이 각각 동시 예약 요청 시 1명씩만 성공하는지 검증한다.
     * n (유저) - 3 (좌석)
     * 분산락으로 변경 후 재검증
     * @throws Exception Exceptions
     */
    @Test
    @DisplayName("동시에 여러명이 여러개의 좌석에 각각 예약 요청 시 한 명씩만 성공한다.")
    void 동시에_여러명이_여러개의_좌석에_각각_예약_요청시_한명씩만_성공한다() throws Exception {
        // given
        Long scheduleId = 2001L;
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<SeatQueryResult> seats = seatQueryService.getSeatsWithAvailability(scheduleId);

        // when
        for (int i = 0; i < threadCount; i++) {
            String userId = "test-user-" + i;
            int index = i % 3;
            Long seatId = seats.get(index).getId();
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    latch.await();
                    try {
                        reserveSeatUseCase.reserveSeat(new ReserveSeatCommand(userId, seatId));
                        successCount.incrementAndGet();
                    } catch (Exception ignored) {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executor));
        }

        latch.countDown();
        for (CompletableFuture<Void> future : futures) {
            future.get();
        }

        // then
        List<Reservation> results = reservationJpaDataRepository.findAll();
        assertThat(results.size()).isEqualTo(3);

    }

    /**
     * 트랜잭션이 커밋된 다음 분산락이 제대로 해제 되는지 검증한다.
     */
    @Test
    @DisplayName("트랜잭션 커밋 이후 분산락이 해제된다.")
    void 트랜잭션_커밋_이후_분산락이_해제된다() {
        // given
        ReserveSeatCommand command = new ReserveSeatCommand("user1", 9821L);

        // when
        reserveSeatUseCase.reserveSeat(command);

        // then
        RLock lock = redissonClient.getLock("lock:seat:9821");
        assertThat(lock.isLocked()).isFalse();
    }

    /**
     * 트랜잭션이 롤백된 다음 분산락이 제대로 해제 되는지 검증한다.
     * - SeatNotFoundException 발생 시킨 후 락 해제 여부 검증
     */
    @Test
    @DisplayName("트랜잭션 롤백 이후 분산락이 해제된다.")
    void 트랜잭션_롤백_이후_분산락이_해제된다() {
        // given
        ReserveSeatCommand command = new ReserveSeatCommand("user1", 1000000L);

        // when
        var throwableAssert = assertThatThrownBy(
            () -> reserveSeatUseCase.reserveSeat(command));

        // then
        throwableAssert.isInstanceOf(SeatNotFoundException.class);
        RLock lock = redissonClient.getLock("lock:seat:1000000");
        assertThat(lock.isLocked()).isFalse();
    }
}