package seong.onlinestudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OnlineStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineStudyApplication.class, args);
    }

}
