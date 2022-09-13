package ru.radius17.reg_auth.service;

import liquibase.repackaged.org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.radius17.reg_auth.entity.Notification;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.repository.NotificationRepository;
import ru.radius17.reg_auth.utils.NotificationSender;

import java.time.*;

@Service
public class NotificationService {
    @Autowired
    private Environment env;
    @Autowired
    NotificationRepository mainRepository;
    @Autowired
    NotificationSender notificationSender;

    public int[] getUint8Key(){
        return notificationSender.getUint8Key();
    }

    public String sendPush(User user, String subject, String message){
        int statusCode = 500;
        String statusMessage = "";

        try {
            statusCode = notificationSender.sendPush(user, subject, message);
        } catch (Exception e){
            statusMessage = ExceptionUtils.getMessage(e);
        }

        System.out.print("Status: " + statusCode);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setStatus(statusCode);
        notification.setNotificationType("push");
        notification.setDateTime(LocalDateTime.now());
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setDescription(statusMessage);

        String logging_level = env.getProperty("spring.application.notifications.logging.level");

        switch (logging_level){
            case "error":
                if(statusCode != 201){
                    try {
                        mainRepository.save(notification);
                    } catch (Exception e){
                        System.out.print(ExceptionUtils.getStackTrace(e));
                    }
                }
                break;
            case "debug":
                try {
                    mainRepository.save(notification);
                } catch (Exception e){
                    System.out.print(ExceptionUtils.getStackTrace(e));
                }
                break;
            default:
                break;
        }
        return String.valueOf(statusCode);
    }
    public String sendMail(User user, String subject, String message){
        int statusCode = 500;
        String statusMessage = "";

        try {
            statusCode = notificationSender.sendMail(user, subject, message);
        } catch (Exception e) {
            statusMessage = ExceptionUtils.getMessage(e);
        }

        System.out.println("Status: " + statusCode + ". Status message: " + statusMessage);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setStatus(statusCode);
        notification.setNotificationType("mail");
        notification.setDateTime(LocalDateTime.now());
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setDescription(statusMessage);

        String logging_level = env.getProperty("spring.application.notifications.logging.level");

        switch (logging_level){
            case "error":
                if(statusCode != 200){
                    try {
                        mainRepository.save(notification);
                    } catch (Exception e){
                        System.out.print(ExceptionUtils.getStackTrace(e));
                    }
                }
                break;
            case "debug":
                try {
                    mainRepository.save(notification);
                } catch (Exception e){
                    System.out.print(ExceptionUtils.getStackTrace(e));
                }
                break;
            default:
                break;
        }
        return String.valueOf(statusCode);
    }
    public Page<Notification> getAllFilteredAndPaginated(Specification specification, Pageable pageable) {
        return mainRepository.findAll(specification, pageable);
    }

}
