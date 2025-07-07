package kr.hhplus.be.server.concert.infrastructure.persistence;

import java.time.LocalDateTime;
import kr.hhplus.be.server.concert.application.SoldOutStateManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 를 사용한 매진 상태 서비스 구현체
 */
@Component
public class RedisSoldOutStateManager implements SoldOutStateManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SOLD_OUT_STATE_KEY_PREFIX = "soldout:concert:";

    public RedisSoldOutStateManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 매진 상태 저장
     * - 기존 매진이 아닐때만 저장 (SETNX)
     * - 레디스 명령 실패도 false 반환
     * @param concertId 콘서트 식별자
     * @param soldOutTime 매진 시각
     * @return boolean 저장 성공 여부
     */
    @Override
    public boolean addIfAbsent(Long concertId, LocalDateTime soldOutTime) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
            .setIfAbsent(SOLD_OUT_STATE_KEY_PREFIX + concertId, soldOutTime.toString()));
    }
}
