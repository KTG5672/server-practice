package kr.hhplus.be.server.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 예약 도메인
 * - HOLD 상태를 포함하는 정적 팩토리 메서드 제공 (hold)
 * - 유효 상태 검사 기능 제공
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "price", nullable = false)
    private int price;

    protected Reservation() {}

    public Reservation(Long id, String userId, Long seatId, ReservationStatus status, int price) {
        this.id = id;
        this.userId = userId;
        this.seatId = seatId;
        this.status = status;
        this.price = price;
    }

    /**
     * 예약 상태 HOLD 로 고정, 정적 팩토리 메서드
     * @param userId : 유저 식별자
     * @param seatId : 좌석 식별자
     * @return Reservation : HOLD 상태의 새로운 객체
     */
    public static Reservation holdOf(String userId, Long seatId, int price) {
        return new Reservation(null, userId, seatId, ReservationStatus.HOLD, price);
    }

    /**
     * 유효한 예약 상태인지 확인 기능
     * @return boolean : 유효한 상태인지
     */
    public boolean isActive() {
        return this.status != ReservationStatus.CANCELLED;
    }

    /**
     * 예약 상태를 Completed 로 변경
     */
    public void completed() {
        this.status = ReservationStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public int getPrice() {
        return price;
    }
}
