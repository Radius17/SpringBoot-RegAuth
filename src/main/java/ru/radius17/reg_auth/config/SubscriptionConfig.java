package ru.radius17.reg_auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.radius17.reg_auth.utils.NotificationSender;

@Configuration
public class SubscriptionConfig {

    @Autowired
    NotificationSender notificationSender;

    @Bean
    public int[] publicKeyUint8Array(){
        return notificationSender.getUint8Key();
    }

}
