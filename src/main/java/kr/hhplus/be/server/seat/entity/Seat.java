package kr.hhplus.be.server.seat.entity;

public class Seat {

    private Long id;
    private Long scheduleId;
    private String zone;
    private int no;
    private int price;

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
