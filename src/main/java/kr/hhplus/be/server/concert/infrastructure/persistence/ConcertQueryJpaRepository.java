package kr.hhplus.be.server.concert.infrastructure.persistence;

import java.util.List;
import kr.hhplus.be.server.concert.application.dto.ConcertQueryResult;
import kr.hhplus.be.server.concert.application.query.ConcertQueryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ConcertQueryJpaRepository implements ConcertQueryRepository {

    private final JpaConcertQueryRepository jpaConcertQueryRepository;

    public ConcertQueryJpaRepository(JpaConcertQueryRepository jpaConcertQueryRepository) {
        this.jpaConcertQueryRepository = jpaConcertQueryRepository;
    }

    @Override
    public List<ConcertQueryResult> findAvailableConcerts() {
        return jpaConcertQueryRepository.findAvailableConcerts();
    }
}
