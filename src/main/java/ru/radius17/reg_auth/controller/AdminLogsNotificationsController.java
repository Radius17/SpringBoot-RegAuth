package ru.radius17.reg_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.radius17.reg_auth.entity.Notification;
import ru.radius17.reg_auth.service.*;
import ru.radius17.reg_auth.utils.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@SessionAttributes({"notificationListPageRequest", "notificationListSearchCriterias"})
@RequestMapping(value = "/admin/logs/notifications")
public class AdminLogsNotificationsController {

    private final String modifyTemplate = "admin/logs/notifications/modify";
    private final String listTemplate = "admin/logs/notifications/list";
    private final String redirectToList = "redirect:/admin/logs/notifications";
    private final String redirectToListPage = "redirect:/admin/logs/notifications?page=";

    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private NotificationService mainService;

    @ModelAttribute("notificationListPageRequest")
    public PageRequest getDefaultNotificationListPageRequest() {
        return PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC,"dateTime"));
    }

    @ModelAttribute("notificationListSearchCriterias")
    public ArrayList<SearchCriteria> getDefaultsearchCriterias() {
        ArrayList<SearchCriteria> searchCriterias = new ArrayList<>();
        searchCriterias.add(new SearchCriteria("user",":", "", "user.username", null));
        searchCriterias.add(new SearchCriteria("dateTime1",">=", "", "dateTime", "date"));
        searchCriterias.add(new SearchCriteria("dateTime2","<=", "", "dateTime", "date"));
        searchCriterias.add(new SearchCriteria("status",":", "","", null));
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
                           @ModelAttribute("notificationListSearchCriterias") ArrayList<SearchCriteria> searchCriterias,
                           @ModelAttribute("notificationListPageRequest") PageRequest pageRequest,
                           Model model) {

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
        if(sortByFieldName.equals("user")) sortByFieldName = "user.username";

        // --------------------------------------------------------
        // Build filter
        SearchSpecificationsBuilder builder = new SearchSpecificationsBuilder(allRequestParams, searchCriterias, false);
        model.addAttribute("listInSearch", builder.isListInSearch());
        model.addAttribute("notificationListSearchCriterias", builder.getSearchCriterias());

        // --------------------------------------------------------
        // Get items
        Page<Notification> itemsPage = mainService.getAllFilteredAndPaginated(builder.build(), PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortByFieldName)));
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
        model.addAttribute("notificationListPageRequest", PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortBy)));

        return this.listTemplate;
    }
}