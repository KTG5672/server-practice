package kr.hhplus.be.server.concert.application.query;

import java.util.List;
import kr.hhplus.be.server.concert.application.dto.ConcertQueryResult;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 콘서트 조회 서비스
 */
@Service
public class ConcertQueryService {

    private final ConcertQueryRepository concertQueryRepository;

    public ConcertQueryService(ConcertQueryRepository concertQueryRepository) {
        this.concertQueryRepository = concertQueryRepository;
    }

    /**
     * 예약 가능한 콘서트 목록 조회
     * - 조회 시 캐시에 저장
     * @return List<ConcertQueryResult> 콘서트 목록
     */
    @Cacheable(value = "concerts", key = "'availabled'")
    public List<ConcertQueryResult> findAvailableConcerts() {
        return concertQueryRepository.findAvailableConcerts();
    }

}
