package kr.hhplus.be.server.external.dataplatform;

import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DataPlatformTestConsumer {


    private final CountDownLatch latch = new CountDownLatch(1);
    private ConsumerRecord<String, String> received;

    @KafkaListener(topics = "data-platform-topic", groupId = "test-group")
    public void listen(ConsumerRecord<String, String> record) {
        this.received = record;
        latch.countDown();
    }

    public ConsumerRecord<String, String> getReceived() {
        return received;
    }

}
