package kr.hhplus.be.server.concert.application;

import java.time.LocalDateTime;

public interface SoldOutStateManager {

    boolean addIfAbsent(Long concertId, LocalDateTime soldOutTime);

}
