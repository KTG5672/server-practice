package kr.hhplus.be.server.integration.concert;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import kr.hhplus.be.server.concert.application.ConcertService;
import kr.hhplus.be.server.concert.application.dto.ConcertCreateCommand;
import kr.hhplus.be.server.concert.application.dto.ConcertQueryResult;
import kr.hhplus.be.server.concert.application.query.ConcertQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/sql/mysql/concert-query-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/mysql/clear-test-db.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class ConcertQueryServiceTest {

    @Autowired
    ConcertQueryService concertQueryService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ConcertService concertService;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    /**
     * 예약 가능한 콘서트 목록를 정상적으로 조회 하는지 검증한다.
     * - 전체 6개 중 4개 예약 가능 (티켓 오픈일 <= 현재 <= 공연 시작일)
     */
    @Test
    @DisplayName("예약 가능한 콘서트를 조회한다.")
    void 예약_가능한_콘서트를_조회한다() {
        // given
        // when
        List<ConcertQueryResult> availableConcerts = concertQueryService.findAvailableConcerts();
        // then
        assertThat(availableConcerts.size()).isEqualTo(4);
    }

    /**
     * 예약 가능한 콘서트를 한번 조회시 데이터가 캐시에 정상적으로 저장되는지 검증한다.
     */
    @Test
    @DisplayName("예약 가능한 콘서트를 조회시 캐시에 저장된다.")
    void 예약_가능한_콘서트를_조회시_캐시에_저장된다() {
        // given
        // when
        concertQueryService.findAvailableConcerts();
        // then
        List<ConcertQueryResult> cached = getCachedAvailableConcerts();

        assertThat(cached.size()).isEqualTo(4);
    }

    @SuppressWarnings("unchecked")
    private List<ConcertQueryResult> getCachedAvailableConcerts() {
        Cache cache = cacheManager.getCache("concerts");
        if (cache == null) {
            return Collections.emptyList();
        }
        return Optional.ofNullable((List<ConcertQueryResult>) cache.get("availabled", List.class))
            .orElse(Collections.emptyList());
    }

    /**
     * 콘서트가 추가/변경 되었을 경우 기존 캐시 데이터가 초기화 되는지 검증한다.
     */
    @Test
    @DisplayName("콘서트 정보가 변경되면 캐시를 초기화 한다.")
    void 콘서트_정보가_변경되면_캐시를_초기화_한다() {
        // given
        LocalDate now = LocalDate.now();
        ConcertCreateCommand concertCreateCommand = new ConcertCreateCommand("new-test-concert",
            now, now.plusDays(1));
        // when
        concertQueryService.findAvailableConcerts();
        concertService.createConcert(concertCreateCommand);

        // then
        List<ConcertQueryResult> cached = getCachedAvailableConcerts();
        assertThat(cached).isEmpty();
    }
}