package kr.hhplus.be.server.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "concert_schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "place")
    private String place;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "ticket_open_date_time")
    private LocalDateTime ticketOpenDateTime;

    protected Schedule() {}

    public Long getId() {
        return id;
    }

    public Long getConcertId() {
        return concertId;
    }

    public String getPlace() {
        return place;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getTicketOpenDateTime() {
        return ticketOpenDateTime;
    }
}
