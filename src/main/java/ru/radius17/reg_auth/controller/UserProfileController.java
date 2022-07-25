package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.UserService;

import javax.validation.Valid;

@Controller
public class UserProfileController {

    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String modifyProfile(Model model) throws UsernameNotFoundException {
        User user = userService.getMySelf();
        user.setPassword(null);
        user.setPasswordConfirm(null);
        model.addAttribute("userForm", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        User mySelf = userService.getMySelf();
        if (userForm.getPassword().isEmpty() || userForm.getPasswordConfirm().isEmpty()) {
            userForm.setPassword(mySelf.getPassword());
            userForm.setPasswordConfirm(null);
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, errMess));
            bindingResult.addError(new FieldError("userForm", "passwordConfirm", null, false, null, null, errMess));
        }

        if (bindingResult.hasErrors()) {
            return "profile";
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

        try {
            userService.saveUser(userForm);
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "profile";
        }

        redirectAttributes.addAttribute("infoMessage", ms.getMessage("user.profileSaved", null, LocaleContextHolder.getLocale()));
        return "redirect:/";
    }
}
