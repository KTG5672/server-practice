package kr.hhplus.be.server.reservation.interfaces.gateway;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaDataRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySeatId(Long seatId);
}
