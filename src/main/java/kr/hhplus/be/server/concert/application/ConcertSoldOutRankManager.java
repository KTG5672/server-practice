package kr.hhplus.be.server.concert.application;

import java.util.List;

public interface ConcertSoldOutRankManager {

    void recordSoldOut(Long concertId, long soldOutSeconds);
    List<Long> getTopRankedConcertIds(int limit);

}
