package com.thinkdifferent.convertvideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class ConvertvideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvertvideoApplication.class, args);
    }
}
