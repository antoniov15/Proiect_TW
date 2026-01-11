package org.example.microserviceaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroserviceAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceAccountApplication.class, args);
    }

}
