package kr.hhplus.be.server.reservation.entity;

/**
 * 예약 상태 Enum
 * - HOLD - 임시 배정 (결제 전)
 * - COMPLETED - 완료 (결제 후)
 * - CANCELLED - 취소
 */
public enum ReservationStatus {
    HOLD, COMPLETED, CANCELLED
}
