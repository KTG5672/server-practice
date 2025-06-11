package kr.hhplus.be.server.payment.intefaces.gateway;

import kr.hhplus.be.server.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaDataRepository extends JpaRepository<Payment, Long> {

}
