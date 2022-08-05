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
import ru.radius17.reg_auth.service.*;
import ru.radius17.reg_auth.utils.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@SessionAttributes({"userListPageRequest", "userListSearchCriterias"})
@RequestMapping(value = "/admin/users")
public class AdminUsersController {
    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET, value = "/add")
    public String addUserProfile(Model model) {
        model.addAttribute("userForm", userService.getEmptyUser());
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("selectedRoles", userService.getSelectedRoles(listRoles, userService.getEmptyUser()));
        model.addAttribute("isNewUser", true);
        model.addAttribute("isMySelf", false);
        return "admin/users/profile";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/modify/{id}")
    public String modifyUserProfile(@PathVariable("id") UUID id,
                                    Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("userForm", user);
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("selectedRoles", userService.getSelectedRoles(listRoles, user));
        model.addAttribute("isNewUser", false);
        User mySelf = userService.getMySelf();
        model.addAttribute("isMySelf", mySelf.getId().equals(id));
        return "admin/users/profile";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public String saveUserProfile(@ModelAttribute("userForm") @Valid User userForm,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        List<Role> listRoles = roleService.getAllRoles();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("selectedRoles", userService.getSelectedRoles(listRoles, userForm));

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
            return "admin/users/profile";
        }

        if(!isNewUser){
            User oldUser = userService.getUserById(userForm.getId());
            userForm.setWebPushSubscription(oldUser.getWebPushSubscription());
        }

        try {
            userService.saveUser(userForm);
        } catch (UserServiceException e) {
            if (!e.getConstraintRejectedFieldName().isEmpty())
                bindingResult.rejectValue(e.getConstraintRejectedFieldName(), null, ms.getMessage(e.getConstraintRejectedFieldMessage(), null, LocaleContextHolder.getLocale()));
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "admin/users/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return "admin/users/profile";
        }
        redirectAttributes.addAttribute("infoMessage", ms.getMessage(isNewUser ? "user.addedSuccessfully" : "user.savedSuccessfully", null, LocaleContextHolder.getLocale()));

        return "redirect:/admin/users";
    }

    @ModelAttribute("userListPageRequest")
    public PageRequest getDefaultUserListPageRequest() {
        return PageRequest.of(0, 10, Sort.by(Sort.DEFAULT_DIRECTION,"username"));
    }

    @ModelAttribute("userListSearchCriterias")
    public ArrayList<SearchCriteria> getDefaultsearchCriterias() {
        ArrayList<SearchCriteria> searchCriterias = new ArrayList<>();
        searchCriterias.add(new SearchCriteria("username",":", ""));
        searchCriterias.add(new SearchCriteria("nickname",":", ""));
        searchCriterias.add(new SearchCriteria("email",":", ""));
        searchCriterias.add(new SearchCriteria("phone",":", ""));
        return searchCriterias;
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public String userList(@RequestParam(name = "page", defaultValue = "-1") Integer pageNo,
                           @RequestParam(name = "limit", defaultValue = "-1") Integer pageSize,
                           @RequestParam(name = "sort", defaultValue = "") String sortBy,
                           @RequestParam(name = "direction", defaultValue = "") String sortDir,
                           @RequestParam(name = "infoMessage", required = false) String infoMessage,
                           @RequestParam(name = "errorMessage", required = false) String errorMessage,
                           @RequestParam Map<String,String> allRequestParams,
                           @ModelAttribute("userListSearchCriterias") ArrayList<SearchCriteria> searchCriterias,
                           @ModelAttribute("userListPageRequest") PageRequest pageRequest,
                           Model model) {

        // --------------------------------------------------------
        // Messages from Request
        model.addAttribute("infoMessage", infoMessage);
        model.addAttribute("errorMessage", errorMessage);

        // --------------------------------------------------------
        // Check vars for pageRequest
        if (pageNo < 1) pageNo = pageRequest.getPageNumber() + 1;
        if (pageSize < 1) pageSize = pageRequest.getPageSize();
        if (sortBy.isEmpty()) {
            sortBy = pageRequest.getSort().toString().split(":")[0].trim();
            sortDir = pageRequest.getSort().toString().split(":")[1].trim();
        }
        if (sortDir.isEmpty()){
            sortDir = pageRequest.getSort().toString().split(":")[1].trim();
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(sortDir);
        } catch (Exception e){
            sortDirection = Sort.DEFAULT_DIRECTION;
        }

        // --------------------------------------------------------
        // Adding sort data to model
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDirection.toString());

        // --------------------------------------------------------
        // Now substitute real sort field to sortByFieldName if need
        String sortByFieldName = sortBy;

        // --------------------------------------------------------
        // Filters
        if(!allRequestParams.isEmpty()){
            String buttonPressed = allRequestParams.get("filter-user-submit");
            for (ListIterator iter = searchCriterias.listIterator();iter.hasNext();) {
                SearchCriteria baseSearchCriteria = (SearchCriteria) iter.next();
                String searchCriteriaInRequest = "reset".equals(buttonPressed) ? "" : allRequestParams.get("filter-user-" + baseSearchCriteria.getKey());
                if(searchCriteriaInRequest != null){
                    searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), searchCriteriaInRequest));
                }
            }
        }

        boolean userListInSearch = false;
        SearchSpecificationsBuilder builder = new SearchSpecificationsBuilder(false);
        for (SearchCriteria searchCriteria: searchCriterias){
            if(searchCriteria.getValue() != "") {
                builder.with(searchCriteria.getKey(), searchCriteria.getOperation(), searchCriteria.getValue());
                userListInSearch = true;
            }
        }

        model.addAttribute("userListInSearch", userListInSearch);
        model.addAttribute("userListSearchCriterias", searchCriterias);

        // --------------------------------------------------------
        // Get items
        Page<User> itemsPage = userService.getUsersFilteredAndPaginated(builder.build(), PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortByFieldName)));
        model.addAttribute("itemsPage", itemsPage);

        // --------------------------------------------------------
        // Prepare pageNumbers
        int totalPages = itemsPage.getTotalPages();
        if (totalPages > 0) {
            if (totalPages < pageNo) {
                return "redirect:/admin/users?page=" + totalPages;
            }
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // --------------------------------------------------------
        // Save page and sort attributes between page calls
        model.addAttribute("userListPageRequest", PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortBy)));
        return "admin/users/list";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String deleteUser(@RequestParam(required = true, defaultValue = "") UUID userId,
                             RedirectAttributes redirectAttributes) {

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
        return "redirect:/admin/users";
    }
}