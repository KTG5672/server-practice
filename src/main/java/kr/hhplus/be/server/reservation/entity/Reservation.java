package kr.hhplus.be.server.reservation.entity;

/**
 * 예약 도메인
 * - HOLD 상태를 포함하는 정적 팩토리 메서드 제공 (hold)
 * - 유효 상태 검사 기능 제공
 */
public class Reservation {

    private Long id;
    private String userId;
    private Long seatId;
    private ReservationStatus status;

    public Reservation(Long id, String userId, Long seatId, ReservationStatus status) {
        this.id = id;
        this.userId = userId;
        this.seatId = seatId;
        this.status = status;
    }

    /**
     * 예약 상태 HOLD 로 고정, 정적 팩토리 메서드
     * @param userId : 유저 식별자
     * @param seatId : 좌석 식별자
     * @return Reservation : HOLD 상태의 새로운 객체
     */
    public static Reservation hold(String userId, Long seatId) {
        return new Reservation(null, userId, seatId, ReservationStatus.HOLD);
    }

    /**
     * 유효한 예약 상태인지 확인 기능
     * @return boolean : 유효한 상태인지
     */
    public boolean isActive() {
        return this.status != ReservationStatus.CANCELLED;
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
}
