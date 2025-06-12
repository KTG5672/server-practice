package kr.hhplus.be.server.common.queue.infrastructure;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Objects;
import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
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

}