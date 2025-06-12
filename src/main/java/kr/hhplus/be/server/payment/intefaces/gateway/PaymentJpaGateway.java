package kr.hhplus.be.server.payment.intefaces.gateway;

import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentJpaGateway implements PaymentRepository {

    private final PaymentJpaDataRepository paymentJpaDataRepository;

    public PaymentJpaGateway(PaymentJpaDataRepository paymentJpaDataRepository) {
        this.paymentJpaDataRepository = paymentJpaDataRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaDataRepository.save(payment);
    }
}
