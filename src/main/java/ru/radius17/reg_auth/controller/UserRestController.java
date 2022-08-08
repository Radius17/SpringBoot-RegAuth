package ru.radius17.reg_auth.controller;

import liquibase.repackaged.org.apache.commons.lang3.exception.ExceptionUtils;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.NotificationService;
import ru.radius17.reg_auth.service.UserService;

@RestController
public class UserRestController {

    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    UserService mainService;
    @Autowired
    NotificationService notificationService;

    @PostMapping("/profile/subscribe")
    public String subscribeWebPush(@RequestBody String subscriptionString){
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(subscriptionString);
            // System.out.println(jsonObject);
            User mySelf = mainService.getMySelf();
            mySelf.setWebPushSubscription(subscriptionString);
            mainService.saveObject(mySelf);
        } catch (Exception e) {
            ExceptionUtils.getStackTrace(e);
        }
        return "ok";
    }

    @GetMapping("/profile/subscribe-test")
    public String subscribeTestWebPush(){
        User mySelf = mainService.getMySelf();
        String subject = ms.getMessage("message.test.subscription.subject", null, LocaleContextHolder.getLocale());
        String text = ms.getMessage("message.test.subscription.text", null, LocaleContextHolder.getLocale());
        return notificationService.send(mySelf, subject, text);
    }

}
