package kr.hhplus.be.server.external.dataplatform.sender;

import java.time.LocalDateTime;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatDto {

    private StatEventType eventType;
    private Long paymentId;
    private String userId;
    private Long seatId;
    private Long amount;
    private LocalDateTime paymentAt;
    private PaymentStatus paymentStatus;
}
