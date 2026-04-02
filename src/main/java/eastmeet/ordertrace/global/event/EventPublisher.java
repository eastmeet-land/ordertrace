package eastmeet.ordertrace.global.event;

public interface EventPublisher {
    void publish(String topic, String key, Object event);
}
