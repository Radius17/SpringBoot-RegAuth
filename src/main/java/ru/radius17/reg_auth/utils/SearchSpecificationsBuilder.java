package ru.radius17.reg_auth.utils;

import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SearchSpecificationsBuilder {
    private final List<SearchCriteria> params;
    private final List<SearchCriteria> searchCriterias;
    private final boolean orPredicate;
    private final boolean listInSearch;

    // May be useful for custom request parsing
    public SearchSpecificationsBuilder(ArrayList<SearchCriteria> searchCriterias, boolean orPredicate) {
        this.orPredicate = orPredicate;
        this.params = new ArrayList<>();
        this.searchCriterias = searchCriterias;

        boolean listInSearch = false;
        for (SearchCriteria searchCriteria: searchCriterias){
            if(searchCriteria.getValue() != "") {
                this.with(searchCriteria.getSubstituteField(), searchCriteria.getOperation(), searchCriteria.getValue(), searchCriteria.getSubstituteField(), searchCriteria.getFieldType());
                listInSearch = true;
            }
        }

        this.listInSearch = listInSearch;
    }

    public SearchSpecificationsBuilder(Map<String,String> allRequestParams, ArrayList<SearchCriteria> searchCriterias, boolean orPredicate) {
        this.orPredicate = orPredicate;
        this.params = new ArrayList<>();

        // --------------------------------------------------------
        // Parse request for standart form
        if(!allRequestParams.isEmpty()){
            String buttonPressed = allRequestParams.get("filter-form-submit");
            for (ListIterator iter = searchCriterias.listIterator(); iter.hasNext();) {
                SearchCriteria baseSearchCriteria = (SearchCriteria) iter.next();
                String searchCriteriaInRequest = "reset".equals(buttonPressed) ? "" : allRequestParams.get("filter-form-" + baseSearchCriteria.getKey());
                if(searchCriteriaInRequest != null){
                    if(baseSearchCriteria.getFieldType() == "date") {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
                        LocalDateTime localDateTime;
                        try {
                            if (baseSearchCriteria.getOperation().equalsIgnoreCase("<")) {
                                localDateTime = LocalDateTime.from(LocalDate.parse(searchCriteriaInRequest, dateTimeFormatter).atStartOfDay());
                            } else {
                                localDateTime = LocalDateTime.from(LocalDate.parse(searchCriteriaInRequest, dateTimeFormatter).atStartOfDay());
                            }
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), localDateTime, baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        } catch (Exception e){
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), "", baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        }
                    } else {
                        searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), searchCriteriaInRequest, baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                    }
                }
            }
        }

        this.searchCriterias = searchCriterias;

        // --------------------------------------------------------
        // Build filter
        boolean listInSearch = false;
        for (SearchCriteria searchCriteria: searchCriterias){
            if(searchCriteria.getValue() != "") {
                this.with(searchCriteria.getSubstituteField(), searchCriteria.getOperation(), searchCriteria.getValue(), searchCriteria.getSubstituteField(), searchCriteria.getFieldType());
                listInSearch = true;
            }
        }

        this.listInSearch = listInSearch;
    }

    public SearchSpecificationsBuilder with(String key, String operation, Object value, String substituteField, String fieldType) {
        this.params.add(new SearchCriteria(key, operation, value, substituteField, fieldType, this.orPredicate));
        return this;
    }

    public Specification<Object> build() {
        if (params.size() == 0) {
            return null;
        }

        List<Specification> specs = params.stream()
                .map(SearchSpecification::new)
                .collect(Collectors.toList());

        Specification result = specs.get(0);

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i)
                    .isOrPredicate()
                    ? Specification.where(result).or(specs.get(i))
                    : Specification.where(result).and(specs.get(i));
        }
        return result;
    }

    private class SearchSpecification implements Specification<Object> {

        private final SearchCriteria criteria;

        public SearchSpecification(SearchCriteria searchCriteria) {
            this.criteria = searchCriteria;
        }

        @Override
        public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            String criteriaOperation = criteria.getOperation();

            // --------------------------------------------------------
            // Check for path like user.username
            String[] criteriaKeys = criteria.getKey().split("\\.");
            Path criteriaPath;
            if(criteriaKeys.length == 2) {
                criteriaPath = root.get(criteriaKeys[0]).get(criteriaKeys[1]);
            } else {
                criteriaPath = root.get(criteriaKeys[0]);
            }

            if (criteriaOperation.equalsIgnoreCase(">")) {
                if (criteriaPath.getJavaType() == LocalDateTime.class) {
                    return criteriaBuilder.greaterThanOrEqualTo(
                            criteriaPath,
                            (LocalDateTime) criteria.getValue()
                    );
                } else {
                    return criteriaBuilder.greaterThanOrEqualTo( criteriaPath, criteria.getValue().toString() );
                }
            } else if (criteriaOperation.equalsIgnoreCase("<")) {
                if (criteriaPath.getJavaType() == LocalDateTime.class) {
                    return criteriaBuilder.lessThanOrEqualTo(
                            criteriaPath,
                            (LocalDateTime) criteria.getValue()
                    );
                } else {
                    return criteriaBuilder.lessThanOrEqualTo( criteriaPath, criteria.getValue().toString() );
                }
            } else if (criteriaOperation.equalsIgnoreCase(":")) {
                if (criteriaPath.getJavaType() == String.class) {
                    return criteriaBuilder.like(
                            criteriaPath,
                            "%" + criteria.getValue() + "%"
                    );
                } else {
                    return criteriaBuilder.equal(
                            criteriaPath,
                            criteria.getValue()
                    );
                }
            }
            return null;
        }
    }
}
