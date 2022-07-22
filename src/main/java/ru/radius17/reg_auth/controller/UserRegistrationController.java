package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.RoleService;
import ru.radius17.reg_auth.service.UserService;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class UserRegistrationController {

    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", userService.getEmptyUser());
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult, Model model) {
        // System.out.println("BINDING " + bindingResult);
        if (userForm.getPassword().isEmpty() || userForm.getPasswordConfirm().isEmpty()) {
            String errMess = ms.getMessage("user.passwordCannotBeEmpty", null, LocaleContextHolder.getLocale());
            bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, errMess));
            bindingResult.addError(new FieldError("userForm", "passwordConfirm", null, false, null, null, errMess));
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, errMess));
            bindingResult.addError(new FieldError("userForm", "passwordConfirm", null, false, null, null, errMess));
        }

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userForm.setId(null);
        userForm.setDescription("");
        userForm.setRoles(Collections.singleton(roleService.getDefaultRole()));
        userForm.setEnabled(true);
        if (!userService.addUser(userForm)) {
            model.addAttribute("formErrorMessage", ms.getMessage("registration.error", null, LocaleContextHolder.getLocale()));
            return "registration";
        }
        return "redirect:/";
    }
}
