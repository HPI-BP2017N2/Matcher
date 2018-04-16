package de.hpi.matcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class MatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatcherApplication.class, args);
    }
}
