package ru.radius17.reg_auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.radius17.reg_auth.service.NotificationService;

@Configuration
public class SubscriptionConfig {

    @Autowired
    NotificationService notificationService;

    @Bean
    public int[] publicKeyUint8Array(){
        return notificationService.getUint8Key();
    }

}
