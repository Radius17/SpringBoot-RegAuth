package ru.radius17.reg_auth.service;

import liquibase.repackaged.org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.radius17.reg_auth.entity.Notify;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.repository.NotifyRepository;
import ru.radius17.reg_auth.utils.NotificationSender;

import java.time.*;
import java.util.SimpleTimeZone;

@Service
public class NotifyService {
    @Autowired
    private Environment env;
    @Autowired
    NotificationSender notificationSender;
    @Autowired
    NotifyRepository notifyRepository;

    public int[] getUint8Key(){
        return notificationSender.getUint8Key();
    }

    public String send(User user, String subject, String message){
        int statusCode = 500;
        String statusMessage = "";

        try {
            statusCode = notificationSender.send(user, subject, message);
        } catch (Exception e){
            statusMessage = ExceptionUtils.getMessage(e);
        }

        System.out.print("Status: " + statusCode);

        Notify notify = new Notify();
        notify.setUser(user);
        notify.setStatus(statusCode);
        notify.setLocalDateTime(ZonedDateTime.now(ZoneId.of(ZoneId.systemDefault().getId())));
        notify.setSubject(subject);
        notify.setMessage(message);
        notify.setDescription(statusMessage);

        String logging_level = env.getProperty("spring.application.notifications.logging.level");

        switch (logging_level){
            case "error":
                if(statusCode != 201){
                    try {
                        notifyRepository.save(notify);
                    } catch (Exception e){
                        System.out.print(ExceptionUtils.getStackTrace(e));
                    }
                }
                break;
            case "debug":
                try {
                    notifyRepository.save(notify);
                } catch (Exception e){
                    System.out.print(ExceptionUtils.getStackTrace(e));
                }
                break;
            default:
                break;
        }
        return String.valueOf(statusCode);
    }
}
