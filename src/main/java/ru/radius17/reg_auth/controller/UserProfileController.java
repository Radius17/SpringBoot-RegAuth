package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.UserService;

import javax.validation.Valid;

@Controller
public class UserProfileController {

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
    public String saveProfile(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult, Model model) {
        User mySelf = userService.getMySelf();
        if (userForm.getPassword().isEmpty() || userForm.getPasswordConfirm().isEmpty()){
            userForm.setPassword(mySelf.getPassword());
            userForm.setPasswordConfirm(null);
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())){
            bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, "Пароли не совпадают"));
            bindingResult.addError(new FieldError("userForm", "passwordConfirm", null, false, null, null, "Пароли не совпадают"));
        }

        if (bindingResult.hasErrors()) {
            return "profile";
        }

        userForm.setId(mySelf.getId());
        userForm.setUsername(mySelf.getUsername());
        userForm.setDescription(mySelf.getDescription());
        userForm.setRoles(mySelf.getRoles());
        userForm.setEnabled(mySelf.getEnabled());
        if (!userService.saveUser(userForm)){
            model.addAttribute("formErrorMessage", "Ошибка сохранения.");
            return "profile";
        }
        return "redirect:/";
    }
}
