package kr.hhplus.be.server.seat.infrastructure.persistence;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import kr.hhplus.be.server.seat.application.dto.SeatCountQueryResult;
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

    @Query(value = """
        select count(distinct seat.id) as totalCount
            , coalesce(sum(case when reservation.status = 'COMPLETED' then 1 else 0 end), 0) as completedCount
        from concerts concert
        join concert_schedules schedule on concert.id = schedule.concert_id
        join concert_seats seat on schedule.id = seat.schedule_id
        left join reservations reservation on seat.id = reservation.seat_id
        where concert.id = :concertId
    """, nativeQuery = true)
    SeatCountQueryResult findSeatsCountByConcertId(@Param("concertId") Long concertId);

}
