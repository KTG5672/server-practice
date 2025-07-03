package kr.hhplus.be.server.common.queue.infrastructure;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenManager;
import kr.hhplus.be.server.common.queue.application.QueueTokenStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis 를 사용한 대기열 토큰 관리 서비스 구현체
 * - 토큰 발급 기능 (대기열 등록, 토큰 정보 반환)
 * - 토큰 정보 조회 기능
 * - 대기열에서 토큰 제거 기능
 * - 대기열 Queue에서 활성 Queue로 이동 기능
 */
@Component
public class RedisQueueTokenManager implements QueueTokenManager {

    private static final long ACTIVE_LIMIT = 10_000;
    private static final String TOKEN_KEY_PREFIX = "queue:token:";
    private static final String WAIT_QUEUE_KEY = "wait_queue";
    private static final String ACTIVE_QUEUE_KEY = "active_queue";

    private final RedisTemplate<String, String> redisTemplate;

    private static final DefaultRedisScript<List<?>> QUEUE_MOVE_SCRIPT;

    // Lua script 객체 로딩
    static {
        String script =
            "local activeSize = redis.call('SCARD', KEYS[2]) " +
                "if activeSize >= tonumber(ARGV[1]) then return {} end " +
                "local moveSize = tonumber(ARGV[1]) - activeSize " +
                "local tokens = redis.call('ZRANGE', KEYS[1], 0, moveSize - 1) " +
                "if #tokens == 0 then return {} end " +
                "for i = 1, #tokens do " +
                "  redis.call('ZREM', KEYS[1], tokens[i]) " +
                "  redis.call('SADD', KEYS[2], tokens[i]) " +
                "end " +
                "return tokens";

        QUEUE_MOVE_SCRIPT = new DefaultRedisScript<>();
        QUEUE_MOVE_SCRIPT.setScriptText(script);
    }

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
            leaveQueue(token);
            return new QueueTokenInfo(token, null, null, QueueTokenStatus.EXPIRED);
        }

        Boolean isActive = redisTemplate.opsForSet().isMember(ACTIVE_QUEUE_KEY, token);
        if (Boolean.TRUE.equals(isActive)) {
            return new QueueTokenInfo(token, null, null, QueueTokenStatus.ACTIVE);
        }

        Long waitCount = redisTemplate.opsForZSet().rank(WAIT_QUEUE_KEY, token);
        if (waitCount == null) {
            leaveQueue(token);
            return new QueueTokenInfo(token, null, null, QueueTokenStatus.EXPIRED);
        }

        long totalWaitMinute = (waitCount + 1) * 2;
        return new QueueTokenInfo(token, waitCount, totalWaitMinute, QueueTokenStatus.WAITING);
    }

    /**
     * 대기열/활성 Queue 및 Key-Value 에서 토큰 정보 제거
     * @param token 대기열 토큰
     */
    @Override
    public void leaveQueue(String token) {
        redisTemplate.opsForZSet().remove(WAIT_QUEUE_KEY, token);
        redisTemplate.opsForSet().remove(ACTIVE_QUEUE_KEY, token);
        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
    }

    /**
     * 대기열 Queue에서 활성 Queue로 이동
     * - 원자성을 위하여 lua script 적용
     * - 활성화 Queue의 남은 자리 만큼 한번에 이동 (최대 활성화 수 - 현재 활성화 수)
     */
    public void moveWaitQueueToActiveQueue() {
        redisTemplate.execute(
            QUEUE_MOVE_SCRIPT,
            List.of(WAIT_QUEUE_KEY, ACTIVE_QUEUE_KEY),
            String.valueOf(ACTIVE_LIMIT)
        );
    }
}
