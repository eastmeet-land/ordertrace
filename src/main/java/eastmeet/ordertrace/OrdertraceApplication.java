package eastmeet.ordertrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OrdertraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdertraceApplication.class, args);
    }

}
