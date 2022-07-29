package ru.radius17.reg_auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Base64;

@Configuration
@ComponentScan(basePackages = "ru.radius17.reg_auth.config")
public class SubscriptionConfig {
    @Autowired
    Environment env;

    @Bean
    public int[] publicKeyUint8Array(){
        String public_key = env.getProperty("spring.application.notifications.keys.public");
        byte[] byteArrray = Base64.getUrlDecoder().decode(public_key);
        int[] buffer = new int[byteArrray.length];
        for (int i = 0; i < byteArrray.length; ++i) {
            buffer[i] = Byte.toUnsignedInt(byteArrray[i]);
        }
        return buffer;
    }

}
