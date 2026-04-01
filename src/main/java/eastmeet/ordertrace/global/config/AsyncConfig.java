package eastmeet.ordertrace.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    /**
     * 결제 처리 전용 스레드 풀을 생성합니다.
     *
     * <ul>
     *   <li>corePoolSize(5) - 기본 유지 스레드 수</li>
     *   <li>maxPoolSize(10) - 최대 스레드 수 (큐가 가득 찰 때 확장)</li>
     *   <li>queueCapacity(25) - 대기 큐 크기 (core 초과 요청을 큐에 대기)</li>
     *   <li>threadNamePrefix("payment-") - 로그에서 결제 스레드 식별용 (Kibana 필터링 활용)</li>
     * </ul>
     *
     * @return 결제 처리용 비동기 Executor
     */
    @Bean(name = "paymentExecutor")
    public Executor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("payment-");
        executor.initialize();
        return executor;
    }
}
