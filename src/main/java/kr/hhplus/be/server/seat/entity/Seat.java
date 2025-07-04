package kr.hhplus.be.server.seat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.schedule.entity.Schedule;

@Entity
@Table(name = "concert_seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JoinColumn(name = "schedule_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;

    @Column(name = "zone")
    private String zone;

    @Column(name = "no")
    private int no;

    @Column(name = "price")
    private int price;

    protected Seat() {}

    public Seat(Long id, Schedule schedule, String zone, int no, int price) {
        this.id = id;
        this.schedule = schedule;
        this.zone = zone;
        this.no = no;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getZone() {
        return zone;
    }

    public int getNo() {
        return no;
    }

    public int getPrice() {
        return price;
    }
}
