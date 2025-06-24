package kr.hhplus.be.server.integration.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import kr.hhplus.be.server.point.application.PointUseService;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/sql/mysql/point-concurrency-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
public class PointConcurrencyTest {

    @Autowired
    PointUseService pointUseService;

    @Autowired
    UserRepository userRepository;

    /**
     * 동시 포인트 사용 요청시 포인트의 값이 음수가 되지 않는것을 검증한다.
     * - 낙관적 락 사용으로 결과가 일정하지 않음
     */
    @Test
    @DisplayName("동시에 여러 포인트 사용 요청 시 잔여 포인트는 0과 같거나 커야한다.")
    void 동시에_여러_포인트_사용요청시_잔여_포인트는_0과_같거나_커야한다() throws Exception {
        // given
        // 초기 포인트 10,000
        String userId = "test-user-1";

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointUseService.usePoint(userId, 2000);
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
        userRepository.findById(userId).ifPresent((user) -> {
            // 잔여 포인트는 0보다 같거나 크다
            assertThat(user.getPoint().getAmount()).isGreaterThanOrEqualTo(0);
        });
        int successResult = successCount.get();
        assertThat(successResult).isLessThan(threadCount);
        int failResult = failCount.get();
        assertThat(failResult).isEqualTo(threadCount - successResult);

    }
}
