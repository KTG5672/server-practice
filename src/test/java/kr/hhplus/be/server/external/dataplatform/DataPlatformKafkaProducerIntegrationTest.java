package kr.hhplus.be.server.external.dataplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.external.dataplatform.sender.PaymentStatDto;
import kr.hhplus.be.server.external.dataplatform.sender.StatEventType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest()
@Testcontainers
@ActiveProfiles("test")
class DataPlatformKafkaProducerIntegrationTest {

    DataPlatformKafkaProducer dataPlatformKafkaProducer;

    @Autowired
    @Qualifier(value = "testKafkaTemplate")
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    DataPlatformTestConsumer dataPlatformTestConsumer;

    @BeforeEach
    void setUp() {
        dataPlatformKafkaProducer = new DataPlatformKafkaProducer(kafkaTemplate, new ObjectMapper());
    }

    @Test
    @DisplayName("DataPlatform Kafka 이벤트 발행 테스트")
    void DataPlatform_Kafka_이벤트_발행_테스트() {
        // given
        PaymentStatDto dto = PaymentStatDto.builder()
            .eventType(StatEventType.PAYMENT_SUCCESS).build();
        // when
        dataPlatformKafkaProducer.sendData(dto)
            .block();
        // then
        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(dataPlatformTestConsumer.getReceived()).isNotNull();
                assertThat(dataPlatformTestConsumer.getReceived().value())
                    .contains("PAYMENT_SUCCESS");
            });
    }
}