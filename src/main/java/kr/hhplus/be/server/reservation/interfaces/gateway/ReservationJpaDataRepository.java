package kr.hhplus.be.server.reservation.interfaces.gateway;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaDataRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySeatId(Long seatId);
    List<Reservation> findByStatus(ReservationStatus status);
}
