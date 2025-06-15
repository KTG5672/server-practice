package kr.hhplus.be.server.common.queue.infrastructure;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenManager;
import kr.hhplus.be.server.common.queue.application.QueueTokenStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 를 사용한 대기열 토큰 관리 서비스 구현체
 * - 토큰 발급 기능 (대기열 등록, 토큰 정보 반환)
 * - 토큰 정보 조회 기능
 * - 대기열에서 토큰 제거 기능
 */
@Component
public class RedisQueueTokenManager implements QueueTokenManager {

    private static final long ACTIVE_LIMIT = 10_000;
    private static final String TOKEN_KEY_PREFIX = "queue:token:";
    private static final String WAIT_QUEUE_KEY = "wait_queue";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisQueueTokenManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 대기열 토큰 발행
     * - 발행 후 토큰을 대기열 큐(정렬 Set)에 저장
     * - Key-Value 형식으로 토큰 - 유저 식별자를 저장
     * - 대기열 토큰 정보 반환
     * @param userId 유저 식별자
     * @return QueueTokenInfo 대기열 토큰 정보
     */
    @Override
    public QueueTokenInfo issueToken(String userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForZSet().add(WAIT_QUEUE_KEY, token, System.currentTimeMillis());
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, userId, Duration.ofMinutes(30));
        return getTokenInfo(token);
    }

    /**
     * 대기열 토큰 정보 반환
     * - 대기열에 토큰 정보가 없으면 대기열에서 정보를 제거 후 만료 상태로 반환
     * - 활성 상태인지 확인 (활성화 유저 수 > 대기열 순서 -> WAITING)
     * @param token 대기열 토큰
     * @return QueueTokenInfo 대기열 토큰 정보
     */
    @Override
    public QueueTokenInfo getTokenInfo(String token) {
        Boolean exists = Optional.of(redisTemplate.hasKey(TOKEN_KEY_PREFIX + token)).orElse(false);
        if (!exists) {
            // 만료 되었다고 판단하고 대기열 토큰 제거하고 만료 응답
            leaveQueue(token);
            return new QueueTokenInfo(token, null, null, QueueTokenStatus.EXPIRED);
        }
        Long waitCount = redisTemplate.opsForZSet().rank(WAIT_QUEUE_KEY, token);
        if (waitCount == null) {
            // 토큰이 정상적으로 저장되지 않았다면 제거 후 만료 응답
            leaveQueue(token);
            return new QueueTokenInfo(token, null, null, QueueTokenStatus.EXPIRED);
        }
        QueueTokenStatus status =
            waitCount > ACTIVE_LIMIT ? QueueTokenStatus.WAITING : QueueTokenStatus.ACTIVE;
        long totalWaitMinute = 0;
        if (status == QueueTokenStatus.WAITING) {
            // 총 대기시간(분) = (총 대기열 숫자 - 활성화 유저) x 2
            totalWaitMinute = (waitCount - ACTIVE_LIMIT) * 2;
        }
        return new QueueTokenInfo(token, waitCount, totalWaitMinute, status);
    }

    /**
     * 대기열 큐 및 Key-Value 에서 토큰 정보 제거
     * @param token 대기열 토큰
     */
    @Override
    public void leaveQueue(String token) {
        redisTemplate.opsForZSet().remove(WAIT_QUEUE_KEY, token);
        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
    }
}
