package kr.hhplus.be.server.concert.application.dto;

import lombok.Getter;

@Getter
public class ConcertQueryResult {

    private Long id;
    private String name;
    private String startDate;
    private String endDate;

    protected ConcertQueryResult() {}

    public ConcertQueryResult(Long id, String name, String startDate, String endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
