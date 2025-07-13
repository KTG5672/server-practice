package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.external.dataplatform.sender.PaymentSuccessStatSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentSuccessEventDataPlatformHandler {

    private final PaymentSuccessStatSender paymentSuccessStatSender;

    public PaymentSuccessEventDataPlatformHandler(PaymentSuccessStatSender paymentSuccessStatSender) {
        this.paymentSuccessStatSender = paymentSuccessStatSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentSuccessEvent event) {
        paymentSuccessStatSender.send(event);
    }

}
