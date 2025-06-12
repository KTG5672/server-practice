package kr.hhplus.be.server.reservation.interfaces.gateway;

import java.time.Duration;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisReservationHoldManager implements ReservationHoldManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int RESERVATION_HOLD_EXPIRE_MINUTES = 5;

    public static final String RESERVATION_HOLD_KEY_PREFIX = "reservation:hold:";

    public RedisReservationHoldManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void hold(Long reservationId) {
        String key = RESERVATION_HOLD_KEY_PREFIX + reservationId;
        redisTemplate.opsForValue().set(key, reservationId.toString(), Duration.ofMinutes(RESERVATION_HOLD_EXPIRE_MINUTES));
    }

    @Override
    public boolean isValid(Long reservationId) {
        return redisTemplate.hasKey(RESERVATION_HOLD_KEY_PREFIX + reservationId);
    }
}
