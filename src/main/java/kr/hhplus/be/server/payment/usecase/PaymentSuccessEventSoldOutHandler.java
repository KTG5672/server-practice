package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.concert.application.processor.ConcertSoldOutProcessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentSuccessEventSoldOutHandler {

    private final ConcertSoldOutProcessor concertSoldOutProcessor;

    public PaymentSuccessEventSoldOutHandler(ConcertSoldOutProcessor concertSoldOutProcessor) {
        this.concertSoldOutProcessor = concertSoldOutProcessor;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentSuccessEvent event) {
        Long seatId = event.seatId();
        Long paymentId = event.paymentId();
        concertSoldOutProcessor.process(seatId, paymentId);
    }
}
