package com.jade.envelope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class RedEnvelopesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedEnvelopesApplication.class, args);
    }

}
