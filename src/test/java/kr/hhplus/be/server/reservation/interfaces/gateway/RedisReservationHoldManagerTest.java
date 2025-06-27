package kr.hhplus.be.server.reservation.interfaces.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RedisReservationHoldManagerTest {

    @Autowired
    ReservationHoldManager reservationHoldManager;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    /**
     * hold 메서드
     * 실제 Redis 저장소에 임시배정 상태가 저장되었는지 검증한다.
     */
    @Test
    @DisplayName("임시배정 상태를 Redis에 저장한다.")
    void 예약_임시배정_상태를_Redis에_저장한다() {
        // given
        Long reservationId = 1L;
        // when
        reservationHoldManager.hold(reservationId);

        // then
        assertThat(redisTemplate.hasKey(RedisReservationHoldManager.RESERVATION_HOLD_KEY_PREFIX + reservationId))
            .isTrue();
    }

    /**
     * hold 메서드(TTL)
     * 실제 Redis 저장소에 TTL 설정되어 저장되었는지 검증한다.
     */
    @Test
    @DisplayName("임시배정 상태 저장 시 TTL이 설정된다.")
    void 임시배정_상태_저장시_TTL이_설정된다() {
        // given
        Long reservationId = 1L;
        reservationHoldManager.hold(reservationId);
        String key = RedisReservationHoldManager.RESERVATION_HOLD_KEY_PREFIX + reservationId;

        // when
        Long expire = redisTemplate.getExpire(key);

        // then
        assertThat(expire).isNotNull();
        assertThat(expire).isGreaterThan(0);
    }

    /**
     * isValid 메서드
     * 실제 Redis 저장소에 임시배정 유효 상태인지 검증한다.
     */
    @Test
    @DisplayName("임시배정 상태인지 확인 가능하다.")
    void 임시배정_상태인지_확인_가능하다() {
        // given
        Long reservationId = 1L;
        String key = RedisReservationHoldManager.RESERVATION_HOLD_KEY_PREFIX + reservationId;
        redisTemplate.opsForValue().set(key, reservationId.toString());
        // when
        boolean valid = reservationHoldManager.isValid(reservationId);

        // then
        assertThat(valid).isTrue();
    }
}