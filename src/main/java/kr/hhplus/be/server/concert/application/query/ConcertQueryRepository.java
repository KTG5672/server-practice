package kr.hhplus.be.server.concert.application.query;

import java.util.List;
import kr.hhplus.be.server.concert.application.dto.ConcertQueryResult;

public interface ConcertQueryRepository {

    List<ConcertQueryResult> findAvailableConcerts();

}
