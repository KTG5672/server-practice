package kr.hhplus.be.server.concert.application;

import java.util.List;

public interface SoldOutRankManager {

    void recordSoldOut(Long concertId, long soldOutSeconds);
    List<Long> getTopRankedConcertIds(int limit);

}
