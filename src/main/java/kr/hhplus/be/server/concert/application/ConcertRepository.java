package kr.hhplus.be.server.concert.application;

import java.util.Optional;
import kr.hhplus.be.server.concert.entity.Concert;

public interface ConcertRepository {

    Concert save(Concert concert);
    Optional<Concert> findById(Long id);

}
