package kr.hhplus.be.server.integration.concert;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentCommand;
import kr.hhplus.be.server.payment.usecase.ProcessPaymentUseCase;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.usecase.ReserveSeatUseCase;
import org.awaitility.Awaitility;
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
@Sql(scripts = "/sql/mysql/concert-sold-out-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
public class ConcertSoldOutTest {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    ProcessPaymentUseCase processPaymentUseCase;

    static final String RANK_KEY = "ranking:soldout:concert";
    static final String SOLD_OUT_STATE_KEY_PREFIX = "soldout:concert:";

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    /**
     * 콘서트 매진 시 매진 처리, 매진 랭킹 기록을 비동기 이벤트로 실행 되는지 검증한다.
     */
    @Test
    @DisplayName("결제_진행후 콘서트 매진 시 비동기 이벤트로 매진 처리를 수행한다.")
    void 결제_진행후_콘서트_매진시_비동기_이벤트로_매진_처리를_수행한다() {
        // given
        String userId = "concert-sold-out-test-user-1";
        Long concertId = 10000L;
        List<Long> seatIds = List.of(10001L, 10002L, 10003L);
        List<Long> reservationIds = new ArrayList<>();

        for (Long seatId : seatIds) {
            reservationIds.add(
                reserveSeatUseCase.reserveSeat(new ReserveSeatCommand(userId, seatId)));
        }

        // when
        for (Long reservationId : reservationIds) {
            processPaymentUseCase.processPayment(new ProcessPaymentCommand(userId, reservationId));
        }

        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(redisTemplate.hasKey(SOLD_OUT_STATE_KEY_PREFIX + concertId)).isTrue();
            assertThat(redisTemplate.hasKey(RANK_KEY)).isTrue();
        });
    }

    /**
     * 콘서트 미매진 시 매진처리가 수행되지 않는 것을 검증한다.
     */
    @Test
    @DisplayName("결제 진행 후 콘서트 미매진시 매진 처리 하지 않는다.")
    void 결제_진행후_콘서트_미매진시_매진_처리_하지_않는다() {
        // given
        String userId = "concert-sold-out-test-user-1";
        Long concertId = 10001L;
        List<Long> seatIds = List.of(10004L, 10005L);
        Long completedSeatId = seatIds.get(0);

        Long completedReservationId = reserveSeatUseCase.reserveSeat(new ReserveSeatCommand(userId, completedSeatId));

        // when
        processPaymentUseCase.processPayment(new ProcessPaymentCommand(userId, completedReservationId));

        // then
        Awaitility.await().untilAsserted(() -> assertThat(
            redisTemplate.hasKey(SOLD_OUT_STATE_KEY_PREFIX + concertId)).isFalse());
    }
}
