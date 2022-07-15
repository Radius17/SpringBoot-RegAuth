package ru.radius17.reg_auth.controller;

import ru.radius17.reg_auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminUsersController {
    @Autowired
    private UserService userService;

    @GetMapping("/admin/users")
    public String userList(Model model) {
        model.addAttribute("allUsers", userService.allUsers());
        return "/admin/users";
    }

    @PostMapping("/admin/users")
    public String deleteUser(@RequestParam(required = true, defaultValue = "" ) Long userId,
                              @RequestParam(required = true, defaultValue = "" ) String action,
                              Model model) {
        if (action.equals("delete")){
            userService.deleteUser(userId);
        }
        return "redirect:/admin/users";
    }
}
