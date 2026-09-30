package in.anumati.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnumatiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnumatiApplication.class, args);
    }
}
