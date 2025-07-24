package kr.hhplus.be.server.external.dataplatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class DataPlatformKafkaProducer implements DataPlatformClient {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String DATAFLATFORM_SEND_TOPIC = "data-platform-topic";

    @Override
    public <T> Mono<?> sendData(T data) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(data))
                .flatMap(json -> Mono.fromFuture(kafkaTemplate.send(DATAFLATFORM_SEND_TOPIC, json)))
                .doOnError(throwable -> log.error("Error sending data to kafka topic, data={}", data, throwable));
    }
}
