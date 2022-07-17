package ru.radius17.reg_auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.RoleService;
import ru.radius17.reg_auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class AdminUsersController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET, value = "/admin/users/add")
    public String addUserProfile( Model model){
        model.addAttribute("userForm", userService.getUserById(0L));
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        return "admin/profile";
    }
    @RequestMapping(method = RequestMethod.GET, value = "/admin/users/modify/{id}")
    public String modifyUserProfile(@PathVariable("id") Long id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("userForm", user);
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        return "admin/profile";
    }
    @RequestMapping(method = RequestMethod.POST, value = "/admin/users/save")
    public String saveUserProfile(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult, Model model) {
        User mySelf = userService.getMySelf();
        return "admin/profile";
/*
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
        if (!userService.saveUser(userForm, mySelf)){
            model.addAttribute("formErrorMessage", "Ошибка сохранения.");
            return "profile";
        }
        return "redirect:/";

 */
    }

    @GetMapping("/admin/users")
    public String userList(@RequestParam(name = "page", defaultValue = "1") Integer pageNo,
                           @RequestParam(name = "limit", defaultValue = "10") Integer pageSize,
                           @RequestParam(name = "sort", defaultValue = "username") String sortBy,
                           @RequestParam(name = "infoMessage", required = false) String infoMessage,
                           @RequestParam(name = "errorMessage", required = false) String errorMessage,
                           Model model) {

        model.addAttribute("infoMessage", infoMessage);
        model.addAttribute("errorMessage", errorMessage);

        Page<User> itemsPage = userService.getUsersPaginated(PageRequest.of(pageNo - 1, pageSize, Sort.by(sortBy)));
        model.addAttribute("itemsPage", itemsPage);

        int totalPages = itemsPage.getTotalPages();
        if (totalPages > 0) {
            if(totalPages < pageNo) {
                return "redirect:/admin/users?page="+totalPages;
            }
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
                             RedirectAttributes attributes) {
        // @TODO Запретить удаление себя
        if (action.equals("delete")){
            User mySelf = userService.getMySelf();
            if(mySelf.getId().equals(userId)){
                attributes.addAttribute("errorMessage", "Нельзя удалить самого себя.");
            } else {
                userService.deleteUser(userId);
            }
        }
        return "redirect:/admin/users";
    }
}
