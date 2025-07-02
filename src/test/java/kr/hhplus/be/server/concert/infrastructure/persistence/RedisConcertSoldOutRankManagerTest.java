package kr.hhplus.be.server.concert.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import kr.hhplus.be.server.concert.application.ConcertSoldOutRankManager;
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
class RedisConcertSoldOutRankManagerTest {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    ConcertSoldOutRankManager concertSoldOutRankManager;

    static final String RANK_KEY = "ranking:soldout:concert";

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    /**
     * 매진까지 소요된 시간 대로 랭킹이 레디스에 저장되는지 검증한다.
     */
    @Test
    @DisplayName("매진시 매진시간 기준으로 랭킹이 레디스에 저장된다.")
    void 매진시_매진시간_기준으로_랭킹이_레디스에_저장된다() {
        // given
        // when
        concertSoldOutRankManager.recordSoldOut(1L, 1000L);
        concertSoldOutRankManager.recordSoldOut(2L, 3000L);
        concertSoldOutRankManager.recordSoldOut(3L, 4000L);
        concertSoldOutRankManager.recordSoldOut(4L, 5000L);
        concertSoldOutRankManager.recordSoldOut(5L, 2000L);

        // then
        Set<String> results = redisTemplate.opsForZSet().range(RANK_KEY, 0, -1);
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(5);
        assertThat(results).containsExactly("1", "5", "2", "3", "4");
    }

    /**
     * 매진 시간 기준 랭킹의 상위 N개의 콘서트 식별자를 조회 해오는지 검증한다.
     */
    @Test
    @DisplayName("매진시간 랭킹이 상위인 콘서트 식별자를 조회한다.")
    void 매진시간_랭킹이_상위인_콘서트_식별자를_조회한다() {
        // given
        concertSoldOutRankManager.recordSoldOut(1L, 1000L);
        concertSoldOutRankManager.recordSoldOut(2L, 3000L);
        concertSoldOutRankManager.recordSoldOut(3L, 4000L);
        concertSoldOutRankManager.recordSoldOut(4L, 5000L);
        concertSoldOutRankManager.recordSoldOut(5L, 2000L);
        // when
        List<Long> topRankedConcertIds = concertSoldOutRankManager.getTopRankedConcertIds(5);

        // then
        assertThat(topRankedConcertIds).hasSize(5);
        assertThat(topRankedConcertIds).containsExactly(1L, 5L, 2L, 3L, 4L);
    }

}