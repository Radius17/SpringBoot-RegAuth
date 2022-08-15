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
    private final String modifyTemplate = "admin/users/modify";
    private final String listTemplate = "admin/users/list";
    private final String redirectToList = "redirect:/admin/users";
    private final String redirectToListPage = "redirect:/admin/users?page=";
    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private UserService mainService;
    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET, value = "/add")
    public String addObject(Model model) {
        model.addAttribute("objectForm", mainService.getEmptyObject());
        model.addAttribute("isNewObject", true);
        model.addAttribute("isMySelf", false);

        // --------------------------------------------------------
        // Here we can add some lists to model
        List<Role> listRoles = roleService.getAll();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("selectedRoles", mainService.getSelectedRoles(listRoles, mainService.getEmptyObject()));
        // --------------------------------------------------------

        return this.modifyTemplate;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/modify/{objId}")
    public String modifyObject(@PathVariable("objId") UUID objId, Model model) {
        User object = mainService.getById(objId);
        model.addAttribute("objectForm", object);
        model.addAttribute("isNewObject", false);
        model.addAttribute("isMySelf", mainService.getMySelf().getId().equals(objId));

        // --------------------------------------------------------
        // Here we can add some lists to model
        List<Role> listRoles = roleService.getAll();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("selectedRoles", mainService.getSelectedRoles(listRoles, object));
        // --------------------------------------------------------

        return this.modifyTemplate;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public String saveObject(@ModelAttribute("objectForm") @Valid User objectForm,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        Boolean isNewObject = objectForm.getId() == null;
        model.addAttribute("isNewObject", isNewObject);

        // --------------------------------------------------------
        // Here we can add some lists to model
        List<Role> listRoles = roleService.getAll();
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("selectedRoles", mainService.getSelectedRoles(listRoles, objectForm));
        // --------------------------------------------------------

        User mySelf = mainService.getMySelf();
        if (mySelf.getId().equals(objectForm.getId())) {
            // --------------------------------------------------------
            // It is myself !!! Restrict to change roles !!!
            objectForm.setRoles(mySelf.getRoles());
            // --------------------------------------------------------
            // It is myself !!! Restrict to change enabled !!!
            objectForm.setEnabled(mySelf.getEnabled());
        }

        model.addAttribute("isMySelf", mySelf.getId().equals(objectForm.getId()));

        if (objectForm.getPassword().isEmpty() || objectForm.getPasswordConfirm().isEmpty()) {
            // --------------------------------------------------------
            // One or both of passwords are empty
            if (isNewObject) {
                String errMess = ms.getMessage("user.passwordCannotBeEmpty", null, LocaleContextHolder.getLocale());
                bindingResult.rejectValue("password", null, errMess);
                bindingResult.rejectValue("passwordConfirm", null, errMess);
            } else {
                // --------------------------------------------------------
                // For existing object password was not changed
                User oldObject = mainService.getById(objectForm.getId());
                objectForm.setPassword(oldObject.getPassword());
                objectForm.setPasswordConfirm(null);
            }
        } else if (!objectForm.getPassword().equals(objectForm.getPasswordConfirm())) {
            // --------------------------------------------------------
            // Passwords not equal
            String errMess = ms.getMessage("user.passwordsNotMatch", null, LocaleContextHolder.getLocale());
            bindingResult.rejectValue("password", null, errMess);
            bindingResult.rejectValue("passwordConfirm", null, errMess);
        }

        if(!isNewObject){
            // --------------------------------------------------------
            // Here we can reset some fields
            User oldObject = mainService.getById(objectForm.getId());
            objectForm.setWebPushSubscription(oldObject.getWebPushSubscription());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return this.modifyTemplate;
        }

        try {
            mainService.saveObject(objectForm);
        } catch (BaseServiceException e) {
            if (!e.getConstraintRejectedFieldName().isEmpty())
                bindingResult.rejectValue(e.getConstraintRejectedFieldName(), null, ms.getMessage(e.getConstraintRejectedFieldMessage(), null, LocaleContextHolder.getLocale()));
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return this.modifyTemplate;
        } catch (Exception e) {
            model.addAttribute("errorMessage", ms.getMessage("save.error", null, LocaleContextHolder.getLocale()));
            return this.modifyTemplate;
        }

        redirectAttributes.addAttribute("infoMessage", ms.getMessage(isNewObject ? "user.addedSuccessfully" : "user.savedSuccessfully", null, LocaleContextHolder.getLocale()));

        return this.redirectToList;
    }

    @ModelAttribute("userListPageRequest")
    public PageRequest getDefaultListPageRequest() {
        return PageRequest.of(0, 20, Sort.by(Sort.DEFAULT_DIRECTION,"username"));
    }

    @ModelAttribute("userListSearchCriterias")
    public ArrayList<SearchCriteria> getDefaultSearchCriterias() {
        ArrayList<SearchCriteria> searchCriterias = new ArrayList<>();
        searchCriterias.add(new SearchCriteria("username",":", "", null, null));
        searchCriterias.add(new SearchCriteria("nickname",":", "", null, null));
        searchCriterias.add(new SearchCriteria("email",":", "", null, null));
        searchCriterias.add(new SearchCriteria("phone",":", "", null, null));
        return searchCriterias;
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public String listObjects(@RequestParam(name = "page", defaultValue = "-1") Integer pageNo,
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
        // Build filter
        SearchSpecificationsBuilder builder = new SearchSpecificationsBuilder(allRequestParams, searchCriterias, false);
        model.addAttribute("listInSearch", builder.isListInSearch());
        model.addAttribute("userListSearchCriterias", builder.getSearchCriterias());

        // --------------------------------------------------------
        // Get items
        Page<User> itemsPage = mainService.getAllFilteredAndPaginated(builder.build(), PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortByFieldName)));
        model.addAttribute("itemsPage", itemsPage);

        // --------------------------------------------------------
        // Prepare pageNumbers
        int totalPages = itemsPage.getTotalPages();
        if (totalPages > 0) {
            if (totalPages < pageNo) {
                return this.redirectToListPage + totalPages;
            }
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // --------------------------------------------------------
        // Save page and sort attributes between page calls
        model.addAttribute("userListPageRequest", PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortBy)));

        return this.listTemplate;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String deleteObject(@RequestParam(name = "objId", required = true, defaultValue = "") UUID objId,
                             RedirectAttributes redirectAttributes) {

        User mySelf = mainService.getMySelf();
        if (mySelf.getId().equals(objId)) {
            redirectAttributes.addAttribute("errorMessage", ms.getMessage("user.youCannotDeleteYourSelf", null, LocaleContextHolder.getLocale()));
        } else {
            try {
                mainService.deleteObject(objId);
                redirectAttributes.addAttribute("infoMessage", ms.getMessage("user.deletedSuccessfully", null, LocaleContextHolder.getLocale()));
            } catch (Exception e) {
                System.out.print(e);
                redirectAttributes.addAttribute("errorMessage", ms.getMessage("delete.error", null, LocaleContextHolder.getLocale()));
            }
        }

        return this.redirectToList;
    }
}
