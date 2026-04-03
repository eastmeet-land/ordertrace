package eastmeet.ordertrace.global.slack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class SlackAlertService {

    private static final String KIBANA_DISCOVER_PATH = "/app/discover#/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long THROTTLE_MILLIS = 60_000;

    private final RestClient restClient;
    private final String webhookUrl;
    private final String kibanaBaseUrl;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Long> lastAlertTimeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> throttledCountMap = new ConcurrentHashMap<>();

    public SlackAlertService(
        @Value("${slack.webhook-url:}") String webhookUrl,
        @Value("${kibana.base-url:http://localhost:5601}") String kibanaBaseUrl,
        ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.webhookUrl = webhookUrl;
        this.kibanaBaseUrl = kibanaBaseUrl;
        this.objectMapper = objectMapper;
    }

    @Async("paymentExecutor")
    public void send(SlackAlert alert) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Slack Webhook URL이 설정되지 않았습니다.");
            return;
        }

        ThrottleResult throttleResult = checkThrottle(alert.title());
        if (throttleResult.throttled()) {
            log.info("Slack 알림 throttled - {}", alert.title());
            return;
        }

        String suppressedInfo = throttleResult.suppressedCount() > 0
            ? String.format("\n⚠️ _이전 1분간 동일 알림 %d건 억제됨_", throttleResult.suppressedCount())
            : "";

        String message = String.format("""
            %s *%s*
            ─────────────────
            %s
            *발생 시각:* %s
            *스레드:* %s%s
            ─────────────────
            <%s|📊 Kibana에서 확인>""",
            alert.emoji(),
            alert.title(),
            alert.body(),
            LocalDateTime.now().format(FORMATTER),
            Thread.currentThread().getName(),
            suppressedInfo,
            buildKibanaLink(alert.kibanaKeyword()));

        sendToSlack(message);
    }

    private record ThrottleResult(boolean throttled, int suppressedCount) {}

    private ThrottleResult checkThrottle(String alertType) {
        long now = System.currentTimeMillis();
        Long lastTime = lastAlertTimeMap.get(alertType);
        if (lastTime != null && (now - lastTime) < THROTTLE_MILLIS) {
            throttledCountMap.merge(alertType, 1, Integer::sum);
            return new ThrottleResult(true, 0);
        }
        Integer suppressed = throttledCountMap.remove(alertType);
        lastAlertTimeMap.put(alertType, now);
        int count = suppressed != null ? suppressed : 0;
        if (count > 0) {
            log.warn("Slack 알림 {}건 억제됨 - {}", count, alertType);
        }
        return new ThrottleResult(false, count);
    }

    private void sendToSlack(String message) {
        try {
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(toPayload(message))
                .retrieve()
                .toBodilessEntity();

            log.info("Slack 알림 전송 완료");
        } catch (Exception e) {
            log.warn("Slack 알림 전송 실패");
            log.debug("Slack 전송 실패 상세", e);
        }
    }

    private String buildKibanaLink(String keyword) {
        String escaped = keyword.replace(":", "\\:");
        return kibanaBaseUrl + KIBANA_DISCOVER_PATH
            + "?_a=(query:(language:kuery,query:'message:*" + escaped + "*'))";
    }

    private String toPayload(String message) {
        try {
            Map<String, Object> text = Map.of("type", "mrkdwn", "text", message);
            Map<String, Object> section = Map.of("type", "section", "text", text);
            Map<String, Object> payload = Map.of("blocks", List.of(section));
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Slack payload 생성 실패");
            return "{}";
        }
    }
}
