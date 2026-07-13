package com.playlet.oversea;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.playlet.oversea.dao")
@EnableTransactionManagement
@EnableScheduling
public class PlayletOverseaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayletOverseaServerApplication.class, args);
    }
}
