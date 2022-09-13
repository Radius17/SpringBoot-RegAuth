package ru.radius17.reg_auth.utils;

import com.google.gson.Gson;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.radius17.reg_auth.entity.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

@Component
public class NotificationSender {

    @Autowired
    private Environment env;

    @Autowired
    private JavaMailSender emailSender;

    public int[] getUint8Key(){
        String public_key = env.getProperty("spring.application.notifications.keys.public");
        byte[] byteArrray = Base64.getUrlDecoder().decode(public_key);
        int[] buffer = new int[byteArrray.length];
        for (int i = 0; i < byteArrray.length; ++i) {
            buffer[i] = Byte.toUnsignedInt(byteArrray[i]);
        }
        return buffer;
    }

    public int sendPush(User user, String subject, String text) throws GeneralSecurityException, JoseException, IOException, ExecutionException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());

        String subscriptionJson = user.getWebPushSubscription();
        String public_key = env.getProperty("spring.application.notifications.keys.public");
        String private_key = env.getProperty("spring.application.notifications.keys.private");
        PushService pushService = new PushService(public_key, private_key, subject);
        Subscription subscription = new Gson().fromJson(subscriptionJson, Subscription.class);
        Notification notification = new Notification(subscription, text);
        HttpResponse httpResponse = pushService.send(notification);

        return httpResponse.getStatusLine().getStatusCode();
    }

    public int sendMail(User user, String subject, String text){
        String userMail = user.getEmail();
        String senderMail = env.getProperty("spring.mail.from.email");
        String senderName = env.getProperty("spring.mail.from.name");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderName + " <" + senderMail + ">");
        message.setReplyTo(senderMail);
        message.setTo(userMail);
        message.setSubject(subject);
        message.setText(text);

        emailSender.send(message);

        return 200;
    }
}
