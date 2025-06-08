package kr.hhplus.be.server.payment.usecase;

public record ProcessPaymentCommand(String userId, Long reservationId) {

}
