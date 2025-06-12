package kr.hhplus.be.server.seat.infrastructure.persistence;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatQueryResult;
import kr.hhplus.be.server.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaSeatQueryRepository extends JpaRepository<Seat, Long> {

    @Query(value = """
        select s.id, s.zone, s.no, s.price
        from concert_seats s
        left join reservations r ON s.id = r.seat_id
        where s.schedule_id = :scheduleId
          and (r.id is null or r.status = 'CANCELLED')
    """, nativeQuery = true)
    List<SeatQueryResult> findSeatsWithAvailability(@Param("scheduleId") Long scheduleId);

}
