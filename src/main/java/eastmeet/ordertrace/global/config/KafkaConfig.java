package eastmeet.ordertrace.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    public static final int REPLICA_COUNT = 1;
    public static final int PARTITION_COUNT = 1;

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
            .partitions(PARTITION_COUNT)
            .replicas(REPLICA_COUNT)
            .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC)
            .partitions(PARTITION_COUNT)
            .replicas(REPLICA_COUNT)
            .build();
    }

}
