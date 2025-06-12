package kr.hhplus.be.server.reservation.entity;

/**
 * Hold 상태의 예약 관리 서비스
 */
public interface ReservationHoldManager {

    void hold(Long reservationId);
    boolean isValid(Long reservationId);

}
