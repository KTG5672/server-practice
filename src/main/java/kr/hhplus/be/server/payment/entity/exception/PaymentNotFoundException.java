package kr.hhplus.be.server.payment.entity.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found for id: " + id);
    }

}
