package kr.hhplus.be.server.schedule.application.dto;

import java.time.LocalDateTime;

/**
 *  콘서트 일정 조회 쿼리 결과 DTO
 */
public class ScheduleQueryResult {

    private Long id;
    private String concertName;
    private String place;
    private LocalDateTime startDateTime;
    private LocalDateTime ticketOpenDateTime;

    public ScheduleQueryResult(Long id, String concertName, String place, LocalDateTime startDateTime,
        LocalDateTime ticketOpenDateTime) {
        this.id = id;
        this.concertName = concertName;
        this.place = place;
        this.startDateTime = startDateTime;
        this.ticketOpenDateTime = ticketOpenDateTime;
    }

    public Long getId() {
        return id;
    }

    public String getConcertName() {
        return concertName;
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
