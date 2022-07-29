package ru.radius17.reg_auth.controller;

import liquibase.repackaged.org.apache.commons.lang3.exception.ExceptionUtils;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.UserService;

@RestController
public class UserRestController {

    @Autowired
    UserService userService;

    @PostMapping("/profile/subscribe")
    public String subscribeWebPush(@RequestBody String subscriptionString){
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(subscriptionString);
            System.out.println(jsonObject);
            User mySelf = userService.getMySelf();
            mySelf.setWebPushSubscription(subscriptionString);
            userService.saveUser(mySelf);
        } catch (Exception e) {
            ExceptionUtils.getStackTrace(e);
        }
        return "ok";
    }
}
