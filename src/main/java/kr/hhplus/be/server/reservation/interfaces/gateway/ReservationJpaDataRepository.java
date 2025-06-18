package kr.hhplus.be.server.reservation.interfaces.gateway;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationJpaDataRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySeatId(Long seatId);

    List<Reservation> findByStatus(ReservationStatus status);

    @Modifying
    @Query(
        "update Reservation r set r.status = :newStatus where r.id = :id and r.status = :status"
    )
    int updateReservationStatusByIdAndStatus(@Param("id") Long id,
        @Param("status") ReservationStatus status, @Param("newStatus") ReservationStatus newStatus);
}
