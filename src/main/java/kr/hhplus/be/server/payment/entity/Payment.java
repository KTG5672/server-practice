package kr.hhplus.be.server.payment.entity;

import java.time.LocalDateTime;

/**
 * 결제 도메인
 * - 진행 중 결재 상태 객체를 반환하는 정적 팩토리 메서드 제공
 * - 상태 (결제완료/실패) 변경 기능
 */
public class Payment {

    private Long id;
    private String userId;
    private Long reservationId;
    private long amount;
    private PaymentStatus stats;
    private LocalDateTime paymentAt;


    public Payment(Long id, String userId, Long reservationId, long amount, PaymentStatus stats,
        LocalDateTime paymentAt) {
        this.id = id;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.stats = stats;
        this.paymentAt = paymentAt;
    }

    /**
     * 진행 상태인 Payment 객체를 반환하는 정적 팩토리 메서드
     * @param userId 유저 식별자
     * @param reservationId 예약 식별자
     * @param amount 결제 금액
     * @return 진행 상태인 Payment 객체
     */
    public static Payment processOf(String userId, Long reservationId, long amount) {
        return new Payment(null, userId, reservationId, amount, PaymentStatus.PROCESS, LocalDateTime.now());
    }

    /**
     * 결제 완료 상태로 변경하는 메서드
     */
    public void success() {
        stats = PaymentStatus.SUCCESS;
    }

    /**
     * 결재 실패 상태로 변경하는 메서드
     */
    public void failed() {
        this.stats = PaymentStatus.FAIL;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public long getAmount() {
        return amount;
    }

    public PaymentStatus getStats() {
        return stats;
    }

    public LocalDateTime getPaymentAt() {
        return paymentAt;
    }
}
