package kr.hhplus.be.server.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.hhplus.be.server.concert.entity.Concert;

@Entity
@Table(name = "concert_schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JoinColumn(name = "concert_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Concert concert;

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

    public Concert getConcert() {
        return concert;
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
