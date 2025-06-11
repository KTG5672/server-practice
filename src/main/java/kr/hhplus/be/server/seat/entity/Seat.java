package kr.hhplus.be.server.seat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "concert_seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "zone")
    private String zone;

    @Column(name = "no")
    private int no;

    @Column(name = "price")
    private int price;

    protected Seat() {}

    public Seat(Long id, Long scheduleId, String zone, int no, int price) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.zone = zone;
        this.no = no;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Long getScheduleId() {
        return scheduleId;
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
