package kr.hhplus.be.server.payment.entity.exception;

public class ProcessPaymentFailException extends RuntimeException {
    public ProcessPaymentFailException(Long id, String message) {
        super("payment failed payment id : " + id + " cause : " + message);
    }
}
