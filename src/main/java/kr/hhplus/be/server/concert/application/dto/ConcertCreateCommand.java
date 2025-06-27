package kr.hhplus.be.server.concert.application.dto;

import java.time.LocalDate;
import kr.hhplus.be.server.concert.entity.Concert;
import lombok.Getter;

@Getter
public class ConcertCreateCommand {

    private String name;
    private LocalDate startDate;
    private LocalDate lastDate;

    protected ConcertCreateCommand() {}

    public ConcertCreateCommand(String name, LocalDate startDate, LocalDate lastDate) {
        this.name = name;
        this.startDate = startDate;
        this.lastDate = lastDate;
    }

    public static Concert toNewEntity(ConcertCreateCommand command) {
        String name = command.getName();
        LocalDate startDate = command.getStartDate();
        LocalDate lastDate = command.getLastDate();

        return Concert.create(name, startDate, lastDate);
    }
}
