package kr.hhplus.be.server.concert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "last_date")
    private LocalDate lastDate;

    protected Concert() {}

    private Concert(Long id, String name, LocalDate startDate, LocalDate lastDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.lastDate = lastDate;
    }

    public static Concert create(String name, LocalDate startDate, LocalDate lastDate) {
        return new Concert(null, name, startDate, lastDate);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }
}
