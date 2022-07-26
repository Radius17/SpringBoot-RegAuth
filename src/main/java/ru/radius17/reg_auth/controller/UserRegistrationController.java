package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.RoleService;
import ru.radius17.reg_auth.service.UserService;

import javax.validation.Valid;

@Controller
public class UserRegistrationController {

    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private Environment env;

    @GetMapping("/registration")
    public String registration(Model model) {
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
        model.addAttribute("userForm", userService.getEmptyUser());
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("userForm") @Valid User userForm,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if(Boolean.parseBoolean(env.getProperty("spring.application.registration.disabled"))==true){
            return "redirect:/";
        }
        if (userForm.getPassword().isEmpty() || userForm.getPasswordConfirm().isEmpty()) {
            String errMess = ms.getMessage("user.passwordCannotBeEmpty", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
            bindingResult.rejectValue("passwordConfirm", null, errMess);
            // =========================================================
            // Another variant
            // =========================================================
            // bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, errMess));
            // bindingResult.addError(new FieldError("userForm", "passwordConfirm", null, false, null, null, errMess));
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
            bindingResult.rejectValue("passwordConfirm", null, errMess);
        }

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        // Always new user
        userForm.setId(null);
        // Empty by default
        userForm.setDescription("");
        // Enable by default
        userForm.setEnabled(true);

        try {
            userService.saveUser(userForm);
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("registration.error", null, LocaleContextHolder.getLocale()));
            return "registration";
        }

        redirectAttributes.addAttribute("infoMessage", ms.getMessage("user.registeredSuccessfully", null, LocaleContextHolder.getLocale()));
        return "redirect:/";
    }
}
