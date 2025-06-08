package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 실패 핸들러
 * - 결제 실패 시 트랜잭션을 새로 생성하여 기존 트랜잭션과 분리 처리
 */
@Service
public class PaymentFailHandler {

    private final PaymentRepository paymentRepository;

    public PaymentFailHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제 실패 핸들러
     * - 결제 상태를 FAIL 변경 후 저장
     * - 트랜잭션 전파를 REQUIRES_NEW 로 설정해 트랜잭션 분리
     * @param payment 결제 도메인
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentFailed(Payment payment) {
        payment.failed();
        paymentRepository.save(payment);
    }

}
