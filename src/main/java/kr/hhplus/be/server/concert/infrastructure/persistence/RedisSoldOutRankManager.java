package kr.hhplus.be.server.concert.infrastructure.persistence;

import kr.hhplus.be.server.concert.application.SoldOutRankManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Redis 를 사용한 매진 랭킹 서비스 구현체
 */
@Component
public class RedisSoldOutRankManager implements SoldOutRankManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RANK_KEY = "ranking:soldout:concert";

    public RedisSoldOutRankManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 매진 정보를 레디스 ZSET 에 저장
     * - 콘서트 식별자를 값으로 매진 시간을 스코어로 하여 저장
     * @param concertId 콘서트 식별자
     * @param soldOutSeconds 매진 시간(초)
     */
    @Override
    public void recordSoldOut(Long concertId, long soldOutSeconds) {
        redisTemplate.opsForZSet().add(RANK_KEY, concertId.toString(), soldOutSeconds);
    }

    /**
     * 매진 랭킹을 위에서 부터 N개 조회
     * @param limit 조회 개수 (N)
     * @return List<Long> 콘서트 식별자 리스트
     */
    @Override
    public List<Long> getTopRankedConcertIds(int limit) {
        Set<String> result = redisTemplate.opsForZSet()
            .range(RANK_KEY, 0, limit - 1);
        if (result == null) {
            return Collections.emptyList();
        }
        return result.stream()
            .map(Long::valueOf)
            .toList();
    }
}
