package ru.radius17.reg_auth.utils;

import com.google.gson.Gson;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.radius17.reg_auth.entity.User;

import java.security.Security;
import java.util.Base64;

@Component
public class NotificationSender {

    @Autowired
    private Environment env;

    public int[] getUint8Key(){
        String public_key = env.getProperty("spring.application.notifications.keys.public");
        byte[] byteArrray = Base64.getUrlDecoder().decode(public_key);
        int[] buffer = new int[byteArrray.length];
        for (int i = 0; i < byteArrray.length; ++i) {
            buffer[i] = Byte.toUnsignedInt(byteArrray[i]);
        }
        return buffer;
    }

    public String send(User user, String subject, String message) {
        Security.addProvider(new BouncyCastleProvider());

        try {
            String subscriptionJson = user.getWebPushSubscription();
            String public_key = env.getProperty("spring.application.notifications.keys.public");
            String private_key = env.getProperty("spring.application.notifications.keys.private");
            PushService pushService = new PushService(public_key, private_key, subject);
            Subscription subscription = new Gson().fromJson(subscriptionJson, Subscription.class);
            Notification notification = new Notification(subscription, message);
            HttpResponse httpResponse = pushService.send(notification);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            return String.valueOf(statusCode);
        } catch (Exception e) {
            return String.valueOf(500);
            // return ExceptionUtils.getStackTrace(e);
        }
    }

}
