package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.NotificationService;
import ru.radius17.reg_auth.service.UserService;

@RestController
public class AdminUsersRestController {
    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService mainService;
    @Autowired
    NotificationService notificationService;

    @PostMapping("/admin/users/subscribe-test")
    public String sendTestWebPush( @RequestParam(name = "username") String username ) {
        User user = (User) mainService.loadUserByUsername(username);
        String subject = ms.getMessage("message.test.subscription.for.subject", null, LocaleContextHolder.getLocale()) + " " + user.getUsername();
        String text = ms.getMessage("message.test.subscription.for.text", null, LocaleContextHolder.getLocale()) + " " + user.getUsername();
        return notificationService.sendPush(user, subject, text);
    }
    @PostMapping("/admin/users/mail-test")
    public String sendTestMail( @RequestParam(name = "username") String username ) {
        User user = (User) mainService.loadUserByUsername(username);
        String subject = ms.getMessage("message.test.subscription.for.subject", null, LocaleContextHolder.getLocale()) + " " + user.getUsername();
        String text = ms.getMessage("message.test.subscription.for.text", null, LocaleContextHolder.getLocale()) + " " + user.getUsername();
        return notificationService.sendMail(user, subject, text);
    }
}
