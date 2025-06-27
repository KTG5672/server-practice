package kr.hhplus.be.server.concert.infrastructure.persistence;

import kr.hhplus.be.server.concert.application.ConcertRepository;
import kr.hhplus.be.server.concert.entity.Concert;
import org.springframework.stereotype.Repository;

@Repository
public class ConcertJpaRepository implements ConcertRepository {

    private final ConcertJpaDataRepository concertJpaDataRepository;


    public ConcertJpaRepository(ConcertJpaDataRepository concertJpaDataRepository) {
        this.concertJpaDataRepository = concertJpaDataRepository;
    }

    @Override
    public Concert save(Concert concert) {
        return concertJpaDataRepository.save(concert);
    }
}
