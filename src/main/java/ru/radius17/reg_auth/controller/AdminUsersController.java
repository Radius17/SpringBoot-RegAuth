package ru.radius17.reg_auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class AdminUsersController {
    @Autowired
    private UserService userService;

    @GetMapping("/admin/users")
    public String userList(@RequestParam(name = "page", defaultValue = "1") Integer pageNo,
                           @RequestParam(name = "limit", defaultValue = "10") Integer pageSize,
                           @RequestParam(name = "sort", defaultValue = "username") String sortBy,
                           Model model) {

        Page<User> itemsPage = userService.findPaginated(PageRequest.of(pageNo - 1, pageSize));
        model.addAttribute("itemsPage", itemsPage);

        int totalPages = itemsPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "admin/users";
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
