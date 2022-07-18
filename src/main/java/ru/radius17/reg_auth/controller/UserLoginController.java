package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Locale;

@Controller
public class UserLoginController {

    @Autowired
    private ApplicationContext appContext;

    @RequestMapping("/login")
    public String login(Model model) {
        // =========================================================
        ReloadableResourceBundleMessageSource messageSource = (ReloadableResourceBundleMessageSource) appContext.getBean("messageSource");
        Locale locale = LocaleContextHolder.getLocale();
        String mess =  messageSource.getMessage("index.welcome",null, locale);
        System.out.print(mess);
        // =========================================================
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return "redirect:/";
        }
        return "login";
    }
}
