package kr.hhplus.be.server.external.dataplatform.sender;

import java.time.LocalDateTime;
import kr.hhplus.be.server.external.dataplatform.DataPlatformApiClient;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import kr.hhplus.be.server.payment.entity.exception.PaymentNotFoundException;
import kr.hhplus.be.server.payment.usecase.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 완료 이벤트에 대한 통계 데이터를 DataPlatform 에 전송하는 클래스
 */
@Slf4j
@Component
public class PaymentSuccessStatSender {

    private final DataPlatformApiClient dataPlatformApiClient;
    private final PaymentRepository paymentRepository;


    public PaymentSuccessStatSender(DataPlatformApiClient dataPlatformApiClient,
        PaymentRepository paymentRepository) {
        this.dataPlatformApiClient = dataPlatformApiClient;
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제 성공 이벤트를 수신 받아 통계 데이터를 구성하여 DataPlatform 에 전송하는 메서드
     * - 파라미터로 받은 정보로 전송 DTO 구성
     * - DataPlatformApiClient 사용하여 비동기 방식으로 전송
     * @param paymentSuccessEvent 결제 완료 이벤트
     */
    @Transactional(readOnly = true)
    public void send(PaymentSuccessEvent paymentSuccessEvent) {
        Long paymentId = paymentSuccessEvent.paymentId();
        Long seatId = paymentSuccessEvent.seatId();

        PaymentStatDto sendDto = getPaymentSuccessStatDto(paymentId, seatId);

        dataPlatformApiClient.sendData(sendDto)
            .subscribe(
                success -> {},
                error -> log.error("Payment success stat send error : " + error.getMessage())
            );
    }

    private PaymentStatDto getPaymentSuccessStatDto(Long paymentId, Long seatId) {

        Payment payment = getPayment(paymentId);
        String userId = payment.getUserId();
        LocalDateTime paymentAt = payment.getPaymentAt();
        Long amount = payment.getAmount();
        PaymentStatus status = payment.getStatus();

        return PaymentStatDto.builder()
            .eventType(StatEventType.PAYMENT_SUCCESS)
            .paymentId(paymentId)
            .userId(userId)
            .seatId(seatId)
            .amount(amount)
            .paymentAt(paymentAt)
            .paymentStatus(status)
            .build();
    }

    private Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }
}
