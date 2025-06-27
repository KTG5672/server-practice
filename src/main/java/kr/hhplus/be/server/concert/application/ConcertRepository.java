package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.entity.Concert;

public interface ConcertRepository {

    Concert save(Concert concert);

}
