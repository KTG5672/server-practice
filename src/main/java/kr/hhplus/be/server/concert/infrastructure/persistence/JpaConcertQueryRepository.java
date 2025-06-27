package kr.hhplus.be.server.concert.infrastructure.persistence;

import java.util.List;
import kr.hhplus.be.server.concert.application.dto.ConcertQueryResult;
import kr.hhplus.be.server.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaConcertQueryRepository extends JpaRepository<Concert, Long> {

    @Query(value = """
        select concert.id, concert.name,
                   date_format(concert.start_date, '%Y-%m-%d') as start_date,
                   date_format(concert.last_date, '%Y-%m-%d') as last_date
        from concerts concert
        where exists (
            select 1
            from concert_schedules schedule
            where schedule.concert_id = concert.id
            and now() between schedule.ticket_open_date_time and schedule.start_date_time
        )
        order by concert.start_date
    """, nativeQuery = true)
    List<ConcertQueryResult> findAvailableConcerts();

}
