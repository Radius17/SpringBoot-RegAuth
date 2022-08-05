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
public class AdminLogsController {
    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    private NotificationService notificationService;

    @ModelAttribute("notificationListPageRequest")
    public PageRequest getDefaultNotificationListPageRequest() {
        return PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"dateTime"));
    }

    @ModelAttribute("notificationListSearchCriterias")
    public ArrayList<SearchCriteria> getDefaultsearchCriterias() {
        ArrayList<SearchCriteria> searchCriterias = new ArrayList<>();
        searchCriterias.add(new SearchCriteria("user",":", ""));
        searchCriterias.add(new SearchCriteria("dateTime",":", ""));
        searchCriterias.add(new SearchCriteria("status",":", ""));
        return searchCriterias;
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public String notificationsList(@RequestParam(name = "page", defaultValue = "-1") Integer pageNo,
                           @RequestParam(name = "limit", defaultValue = "-1") Integer pageSize,
                           @RequestParam(name = "sort", defaultValue = "") String sortBy,
                           @RequestParam(name = "direction", defaultValue = "") String sortDir,
                           @RequestParam(name = "infoMessage", required = false) String infoMessage,
                           @RequestParam(name = "errorMessage", required = false) String errorMessage,
                           @RequestParam Map<String,String> allRequestParams,
                           @ModelAttribute("notificationListSearchCriterias") ArrayList<SearchCriteria> searchCriterias,
                           @ModelAttribute("notificationListPageRequest") PageRequest pageRequest,
                           Model model) {

        // --------------------------------------------------------

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

        model.addAttribute("infoMessage", infoMessage);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDirection.toString());

        if(!allRequestParams.isEmpty()){
            String buttonPressed = allRequestParams.get("filter-notification-submit");
            for (ListIterator iter = searchCriterias.listIterator();iter.hasNext();) {
                SearchCriteria baseSearchCriteria = (SearchCriteria) iter.next();
                String searchCriteriaInRequest = "reset".equals(buttonPressed) ? "" : allRequestParams.get("filter-notification-" + baseSearchCriteria.getKey());
                if(searchCriteriaInRequest != null){
                    searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), searchCriteriaInRequest));
                }
            }
        }

        boolean notificationListInSearch = false;
        SearchSpecificationsBuilder builder = new SearchSpecificationsBuilder(false);
        for (SearchCriteria searchCriteria: searchCriterias){
            if(searchCriteria.getValue() != "") {
                builder.with(searchCriteria.getKey(), searchCriteria.getOperation(), searchCriteria.getValue());
                notificationListInSearch = true;
            }
        }

        model.addAttribute("notificationListInSearch", notificationListInSearch);
        model.addAttribute("notificationListSearchCriterias", searchCriterias);

        Page<Notification> itemsPage = notificationService.getNotificationsFilteredAndPaginated(builder.build(), PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortBy)));

        model.addAttribute("itemsPage", itemsPage);

        int totalPages = itemsPage.getTotalPages();
        if (totalPages > 0) {
            if (totalPages < pageNo) {
                return "redirect:/admin/logs/notifications?page=" + totalPages;
            }
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("notificationListPageRequest", PageRequest.of(pageNo - 1, pageSize, Sort.by(sortDirection, sortBy)));
        return "admin/logs/notifications/list";
    }
}