package kr.hhplus.be.server.payment.intefaces.gateway;

import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentJpaGateway implements PaymentRepository {

    @Override
    public Payment save(Payment payment) {
        return null;
    }
}
