package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.UserService;
import ru.radius17.reg_auth.utils.NotificationSender;

@RestController
public class TestSendController {
    @Autowired
    private UserService userService;

    @Autowired
    NotificationSender notificationSender;
    @Autowired
    int[] publicKeyUint8Array;

    @RequestMapping("/notifications/send")
    public String send(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "subject", defaultValue = "Privet") String subject,
            @RequestParam(name = "message", defaultValue = "It's my message") String message
            ) {
        User user = (User) userService.loadUserByUsername(username);
        // notificationSender.getUint8Key();
        return notificationSender.send(user, subject, message);
    }
}
