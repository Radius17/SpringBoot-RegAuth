package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.RoleService;
import ru.radius17.reg_auth.service.UserService;
import ru.radius17.reg_auth.service.BaseServiceException;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class UserController {

    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService mainService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private Environment env;

    @RequestMapping("/login")
    public String loginForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return "redirect:/";
        }
        return "user/login";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        // =========================================================
        // Variant 1 (Required Autowired ApplicationContext appContext;)
        // =========================================================
        // ReloadableResourceBundleMessageSource messageSource = (ReloadableResourceBundleMessageSource) appContext.getBean("messageSource");
        // Locale locale = LocaleContextHolder.getLocale();
        // String mess =  messageSource.getMessage("index.welcome",null, locale);
        // System.out.print(mess);
        // =========================================================
        // Variant 2 (Required Autowired ReloadableResourceBundleMessageSource ms;)
        // =========================================================
        // System.out.print(ms.getMessage("index.welcome", null, LocaleContextHolder.getLocale()));
        // =========================================================
        // Environment use example
        // =========================================================
        if(Boolean.parseBoolean(env.getProperty("spring.application.registration.disabled"))==true){
            return "redirect:/";
        }
        String app_name = env.getProperty("spring.application.name");
        if (app_name != null) {
            System.out.println("=========================================================");
            System.out.println(app_name);
            System.out.println("=========================================================");
        }
        model.addAttribute("userForm", mainService.getEmptyObject());
        return "user/register";
    }

    @PostMapping("/register")
    public String addProfile(@ModelAttribute("userForm") @Valid User userForm,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        if(Boolean.parseBoolean(env.getProperty("spring.application.registration.disabled"))==true){
            return "redirect:/";
        }

        if (userForm.getPassword().isEmpty()) {
            String errMess = ms.getMessage("user.passwordCannotBeEmpty", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
            // =========================================================
            // Another variant
            // =========================================================
            // bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, errMess));
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("passwordConfirm", null, errMess);
        } else if(!mainService.checkPasswordLength(userForm.getPassword())){
            String errMess = ms.getMessage("message.atLeastXCharacters", new Object[]{mainService.getMinPasswordLength()}, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", ms.getMessage("registration.error", null, LocaleContextHolder.getLocale()));
            return "user/register";
        }

        // --------------------------------------------------------
        // Always new user
        userForm.setId(null);
        // Empty by default
        userForm.setDescription("");
        // Default role
        userForm.setRoles(Collections.singleton(roleService.getDefaultObject()));
        // Enable by default
        userForm.setEnabled(true);
        // Empty by default
        userForm.setWebPushSubscription("");

        try {
            mainService.saveObject(userForm);
        } catch (BaseServiceException e) {
            if (!e.getConstraintRejectedFieldName().isEmpty())
                bindingResult.rejectValue(e.getConstraintRejectedFieldName(), null, ms.getMessage(e.getConstraintRejectedFieldMessage(), null, LocaleContextHolder.getLocale()));
            model.addAttribute("errorMessage", ms.getMessage("registration.error", null, LocaleContextHolder.getLocale()));
            return "user/register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("registration.error", null, LocaleContextHolder.getLocale()));
            return "user/register";
        }

        redirectAttributes.addAttribute("infoMessage", ms.getMessage("user.registeredSuccessfully", null, LocaleContextHolder.getLocale()));
        return "redirect:/";
    }

    @GetMapping("/profile/modify")
    public String modifyProfile(Model model) throws UsernameNotFoundException {
        User user = mainService.getMySelf();
        user.setPassword(null);
        user.setPasswordConfirm(null);
        model.addAttribute("userForm", user);
        return "user/profile";
    }

    @PostMapping("/profile/save")
    public String saveProfile(@ModelAttribute("userForm") @Valid User userForm,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        User mySelf = mainService.getMySelf();
        if (userForm.getPassword().isEmpty() && userForm.getPasswordConfirm().isEmpty()) {
            // --------------------------------------------------------
            // One or both of passwords are empty
            userForm.setPassword(mySelf.getPassword());
            userForm.setPasswordConfirm(null);
        } else if (userForm.getPassword().isEmpty() || !userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            // --------------------------------------------------------
            // Passwords not equal
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("passwordConfirm", null, errMess);
        } else if(!mainService.checkPasswordLength(userForm.getPassword())){
            String errMess = ms.getMessage("message.atLeastXCharacters", new Object[]{mainService.getMinPasswordLength()}, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "user/profile";
        }

        // Restrict to change UUID
        userForm.setId(mySelf.getId());
        // Restrict to change username
        userForm.setUsername(mySelf.getUsername());
        // Restrict to change description
        userForm.setDescription(mySelf.getDescription());
        // Restrict to change roles
        userForm.setRoles(mySelf.getRoles());
        // Restrict to change enabled
        userForm.setEnabled(mySelf.getEnabled());
        // Restrict to change PushSubscription
        userForm.setWebPushSubscription(mySelf.getWebPushSubscription());

        try {
            mainService.saveObject(userForm);
        } catch (BaseServiceException e) {
            if (!e.getConstraintRejectedFieldName().isEmpty())
                bindingResult.rejectValue(e.getConstraintRejectedFieldName(), null, ms.getMessage(e.getConstraintRejectedFieldMessage(), null, LocaleContextHolder.getLocale()));
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "user/profile";
        }

        redirectAttributes.addAttribute("infoMessage", ms.getMessage("user.profileSaved", null, LocaleContextHolder.getLocale()));
        return "redirect:/";
    }
}
