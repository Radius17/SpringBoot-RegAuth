package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.service.RoleService;
import ru.radius17.reg_auth.service.UserService;
import ru.radius17.reg_auth.service.UserServiceException;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@SessionAttributes("userListPageRequest")
public class AdminUsersController {
    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET, value = "/admin/users/add")
    public String addUserProfile(Model model) {
        model.addAttribute("userForm", userService.getEmptyUser());
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("isNewUser", true);
        model.addAttribute("isMySelf", false);
        return "admin/profile";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/users/modify/{id}")
    public String modifyUserProfile(@PathVariable("id") UUID id,
                                    Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("userForm", user);
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("isNewUser", false);
        User mySelf = userService.getMySelf();
        model.addAttribute("isMySelf", mySelf.getId().equals(id));
        return "admin/profile";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/admin/users/save")
    public String saveUserProfile(@ModelAttribute("userForm") @Valid User userForm,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);

        Boolean isNewUser = userForm.getId() == null;
        model.addAttribute("isNewUser", isNewUser);

        User mySelf = userService.getMySelf();
        Boolean isMySelf = mySelf.getId().equals(userForm.getId());
        model.addAttribute("isMySelf", isMySelf);

        if (userForm.getPassword().isEmpty() || userForm.getPasswordConfirm().isEmpty()) {
            if (isNewUser) {
                // One of passwords is empty
                String errMess = ms.getMessage("user.passwordCannotBeEmpty", null, LocaleContextHolder.getLocale());
                bindingResult.rejectValue("password", null, errMess);
                bindingResult.rejectValue("passwordConfirm", null, errMess);
            } else {
                // Password was not changed
                userForm.setPassword(mySelf.getPassword());
                userForm.setPasswordConfirm(null);
            }
        } else if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
            // Passwords not equal
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
            bindingResult.rejectValue("passwordConfirm", null, errMess);
        }

        if (bindingResult.hasErrors()) {
            return "admin/profile";
        }

        try {
            userService.saveUser(userForm);
        } catch (UserServiceException e) {
            if (!e.getConstraintRejectedFieldName().isEmpty())
                bindingResult.rejectValue(e.getConstraintRejectedFieldName(), null, ms.getMessage(e.getConstraintRejectedFieldMessage(), null, LocaleContextHolder.getLocale()));
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "admin/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "admin/profile";
        }
        redirectAttributes.addAttribute("infoMessage", ms.getMessage(isNewUser ? "user.addedSuccessfully" : "user.savedSuccessfully", null, LocaleContextHolder.getLocale()));

        return "redirect:/admin/users";
    }

    @ModelAttribute("userListPageRequest")
    public PageRequest getDefaultUserListPageRequest() {
        return PageRequest.of(0, 10, Sort.by("username"));
    }
    @GetMapping("/admin/users")
    public String userList(@RequestParam(name = "page", defaultValue = "-1") Integer pageNo,
                           @RequestParam(name = "limit", defaultValue = "-1") Integer pageSize,
                           @RequestParam(name = "sort", defaultValue = "") String sortBy,
                           @RequestParam(name = "username", defaultValue = "") String username,
                           @RequestParam(name = "infoMessage", required = false) String infoMessage,
                           @RequestParam(name = "errorMessage", required = false) String errorMessage,
                           @ModelAttribute("userListPageRequest") PageRequest pageRequest,
                           Model model) {

        if (pageNo < 1) pageNo = pageRequest.getPageNumber() + 1;
        if (pageSize < 1) pageSize = pageRequest.getPageSize();
        if (sortBy.isEmpty()) sortBy = pageRequest.getSort().toString().split(":")[0];

        model.addAttribute("infoMessage", infoMessage);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("sortBy", sortBy);

        Page<User> itemsPage;
        if(username.isEmpty()){
            itemsPage = userService.getUsersPaginated(PageRequest.of(pageNo - 1, pageSize, Sort.by(sortBy)));
        } else {
            itemsPage = userService.getUsersFilteredByUsernameAndPaginated(username, PageRequest.of(pageNo - 1, pageSize, Sort.by(sortBy)));
        }
        model.addAttribute("itemsPage", itemsPage);

        int totalPages = itemsPage.getTotalPages();
        if (totalPages > 0) {
            if (totalPages < pageNo) {
                return "redirect:/admin/users?page=" + totalPages;
            }
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("userListPageRequest", PageRequest.of(pageNo - 1, pageSize, Sort.by(sortBy)));
        return "admin/users";
    }

    @PostMapping("/admin/users")
    public String deleteUser(@RequestParam(required = true, defaultValue = "") UUID userId,
                             @RequestParam(required = true, defaultValue = "") String action,
                             RedirectAttributes redirectAttributes) {

        if (action.equals("delete")) {
            User mySelf = userService.getMySelf();
            if (mySelf.getId().equals(userId)) {
                redirectAttributes.addAttribute("errorMessage", ms.getMessage("user.youCannotDeleteYourSelf", null, LocaleContextHolder.getLocale()));
            } else {
                try {
                    userService.deleteUser(userId);
                    redirectAttributes.addAttribute("infoMessage", ms.getMessage("user.deletedSuccessfully", null, LocaleContextHolder.getLocale()));
                } catch (Exception e) {
                    System.out.print(e);
                    redirectAttributes.addAttribute("errorMessage", ms.getMessage("delete.error", null, LocaleContextHolder.getLocale()));
                }
            }
        }
        return "redirect:/admin/users";
    }
}