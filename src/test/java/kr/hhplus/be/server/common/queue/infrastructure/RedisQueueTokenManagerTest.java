package kr.hhplus.be.server.common.queue.infrastructure;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RedisQueueTokenManagerTest {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisQueueTokenManager tokenManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    /**
     * 토큰 발행시 ZSet(정렬 Set)에 토큰 정보를 저장하고, key-value 형식에 토큰-유저 식별자를 저장하는지 검증한다.
     */
    @Test
    @DisplayName("토큰 발급 시 대기열 큐에 저장된다")
    void issueToken_storesToRedis() {
        String userId = "user-abc";
        QueueTokenInfo info = tokenManager.issueToken(userId);

        assertThat(info.token()).isNotNull();
        assertThat(info.status()).isIn(QueueTokenStatus.ACTIVE, QueueTokenStatus.WAITING);

        assertThat(redisTemplate.opsForValue().get("queue:token:" + info.token()))
            .isEqualTo(userId);

        assertThat(redisTemplate.opsForZSet().rank("wait_queue", info.token()))
            .isNotNull();
    }

    /**
     * 토큰 정보 조회 시 상태가 정상적으로 반환되는지 검증한다.
     */
    @Test
    @DisplayName("토큰 정보 조회 시 상태가 반환된다.")
    void 토큰_정보_조회시_상태가_반환된다() {
        String userId = "user-xyz";
        QueueTokenInfo issued = tokenManager.issueToken(userId);

        QueueTokenInfo info = tokenManager.getTokenInfo(issued.token());

        assertThat(info.token()).isEqualTo(issued.token());
        assertThat(info.status()).isIn(QueueTokenStatus.ACTIVE, QueueTokenStatus.WAITING);
        assertThat(info.totalWaitMinute()).isGreaterThanOrEqualTo(0);
    }

    /**
     * 토큰을 대기열에서 제거하거나 만료되면 만료 상태로 반환하는지 검증한다.
     */
    @Test
    @DisplayName("토큰 만료 또는 제거되면 만료 상태로 반환한다")
    void 토큰_만료_또는_제거되면_만료_상태로_반환한다() {
        String userId = "user-expired";
        QueueTokenInfo issued = tokenManager.issueToken(userId);

        tokenManager.leaveQueue(issued.token());

        QueueTokenInfo info = tokenManager.getTokenInfo(issued.token());

        assertThat(info.status()).isEqualTo(QueueTokenStatus.EXPIRED);
    }


    /**
     * 대기열 Queue 에서 활성 Queue 로 한번에 이동 되는지 검증한다.
     */
    @Test
    @DisplayName("대기열 Queue에서 활성 Queue로 한번에 이동한다.")
    void 대기열_Queue_에서_활성_Queue_로_한번에_이동한다() {
        // given
        IntStream.range(0, 5).forEach(i -> {
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForZSet().add("wait_queue", token, System.currentTimeMillis() + i);
            redisTemplate.opsForValue().set("queue:token:" + token, "user" + i);
        });

        // when
        tokenManager.moveWaitQueueToActiveQueue();

        // then
        Set<String> actives = redisTemplate.opsForSet().members("active_queue");
        assertThat(actives).isNotNull().isNotEmpty().hasSize(5);
    }

    /**
     * 활성 Queue 에 활성화된 토큰은 최대 활성수 (10,000) 를 넘지 않는지 검증한다.
     */
    @Test
    @DisplayName("활성 Queue는_최대_활성_수를_초과하지_않는다.")
    void 활성_Queue는_최대_활성_수를_초과하지_않는다() {
        // given
        Set<TypedTuple<String>> waitQueueInputs = new HashSet<>();
        Map<String, String> valueInputs = new HashMap<>();
        IntStream.range(0, 10_005).forEach(i -> {
            String token = UUID.randomUUID().toString();
            waitQueueInputs.add(new DefaultTypedTuple<>(token, (double) (System.currentTimeMillis() + i)));
            valueInputs.put("queue:token:" + token, "user" + i);
        });
        redisTemplate.opsForZSet().add("wait_queue", waitQueueInputs);
        redisTemplate.opsForValue().multiSet(valueInputs);

        // when
        tokenManager.moveWaitQueueToActiveQueue();
        // 한번 더 실행
        tokenManager.moveWaitQueueToActiveQueue();

        // then
        Set<String> actives = redisTemplate.opsForSet().members("active_queue");
        assertThat(actives).isNotNull().isNotEmpty().hasSize(10_000);
    }

}