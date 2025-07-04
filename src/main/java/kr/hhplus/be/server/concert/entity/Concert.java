package kr.hhplus.be.server.concert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import kr.hhplus.be.server.schedule.entity.Schedule;

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

    @OneToMany(mappedBy = "concert")
    private List<Schedule> schedules;

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

    public LocalDateTime getEarliestTicketOpenDateTime() {
        Optional<LocalDateTime> min = schedules.stream()
            .map(Schedule::getTicketOpenDateTime)
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo);
        return min.orElse(null);
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

    public List<Schedule> getSchedules() {
        return schedules;
    }
}
