package kr.hhplus.be.server.seat.infrastructure.persistence;

import kr.hhplus.be.server.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatJpaDataRepository extends JpaRepository<Seat, Long> {

}
