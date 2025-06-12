package kr.hhplus.be.server.seat.application.dto;

/**
 * 좌석 조회 쿼리 결과(예약 가능 여부포함) DTO
 */
public class SeatQueryResult {

    private Long id;
    private String zone;
    private int no;
    private int price;
    private Boolean available;

    public SeatQueryResult(Long id, String zone, int no, int price) {
        this(id, zone, no, price, null);
    }

    public SeatQueryResult(Long id, String zone, int no, int price, Boolean available) {
        this.id = id;
        this.zone = zone;
        this.no = no;
        this.price = price;
        this.available = available;
    }

    public Long getId() {
        return id;
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

    public Boolean isAvailable() {
        return available;
    }
}
