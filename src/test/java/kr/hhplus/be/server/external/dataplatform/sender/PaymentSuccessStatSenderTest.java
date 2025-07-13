package kr.hhplus.be.server.external.dataplatform.sender;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.hhplus.be.server.external.dataplatform.DataPlatformApiClient;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import kr.hhplus.be.server.payment.usecase.PaymentSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PaymentSuccessStatSenderTest {

    @Mock
    DataPlatformApiClient dataPlatformApiClient;

    @Mock
    PaymentRepository paymentRepository;

    PaymentSuccessStatSender paymentSuccessStatSender;

    @BeforeEach
    void setUp() {
        paymentSuccessStatSender = new PaymentSuccessStatSender(dataPlatformApiClient, paymentRepository);
    }

    /**
     * PaymentSuccessStatSender.send -> DataPlatformApiClient.sendData
     * 흐름으로 정상적으로 호출하는지 검증한다.
     */
    @Test
    @DisplayName("payment success stat sender가 정상적으로 data platform api client를 호출한다.")
    void payment_success_stat_sender가_정상적으로_data_platform_api_client를_호출한다() {
        // given
        long paymentId = 1L;
        Long seatId = 1L;
        Payment testPayment = new Payment(paymentId, "test-user", null, 10000L, PaymentStatus.SUCCESS,
            null);
        PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent(paymentId, seatId);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(dataPlatformApiClient.sendData(any(PaymentStatDto.class))).thenReturn(Mono.empty());

        // when
        paymentSuccessStatSender.send(paymentSuccessEvent);

        // then
        verify(dataPlatformApiClient, times(1)).<PaymentStatDto>sendData(argThat(arg ->
            arg.getPaymentId().equals(paymentId) && arg.getSeatId().equals(seatId)
            && arg.getPaymentStatus().equals(PaymentStatus.SUCCESS) && arg.getAmount().equals(10000L)
        ));
    }


}