package ru.radius17.reg_auth.controller;

import org.springframework.validation.FieldError;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult, Model model) {
        // System.out.println("BINDING " + bindingResult);
        if (userForm.getPassword().isEmpty() || userForm.getPasswordConfirm().isEmpty()){
            bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, "Пароль не может быть пустым"));
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())){
            bindingResult.addError(new FieldError("userForm", "password", null, false, null, null, "Пароли не совпадают"));
        }

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userForm.setId(null);
        userForm.setComments("");
        userForm.setRoles(Collections.singleton(new Role(1L, "ROLE_USER")));

        if (!userService.addUser(userForm)){
            model.addAttribute("registrationError", "Ошибка регистрации.");
            return "registration";
        }
        return "redirect:/";
    }
}
