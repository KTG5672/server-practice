package kr.hhplus.be.server.payment.usecase;

public record PaymentSuccessEvent(Long paymentId, Long seatId) {

}
