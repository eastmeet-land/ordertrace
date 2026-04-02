package eastmeet.ordertrace.global.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka 이벤트 발행 실패 - topic: {}, key: {}, error: {}", topic, key, ex.getMessage());
            } else {
                log.info("Kafka 이벤트 발행 - topic: {}, key: {}, offset: {}", topic, key, result.getRecordMetadata().offset());
            }
        });
    }

}
