package ru.radius17.reg_auth.utils;

import lombok.Getter;
import org.hibernate.query.criteria.internal.path.PluralAttributePath;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.BigInteger;
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
                        try {
                            LocalDate localDate = LocalDate.parse(searchCriteriaInRequest, dateTimeFormatter);
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), localDate, baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        } catch (Exception e) {
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), "", baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        }
                    } else if(baseSearchCriteria.getFieldType() == "decimal") {
                        try {
                            BigDecimal decimalVal = BigDecimal.valueOf(Double.valueOf(searchCriteriaInRequest));
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), decimalVal, baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        } catch (Exception e) {
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), "", baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        }
                    } else if(baseSearchCriteria.getFieldType() == "integer") {
                        try {
                            BigInteger intVal = BigInteger.valueOf(Long.valueOf(searchCriteriaInRequest));
                            searchCriterias.set(iter.previousIndex(), new SearchCriteria(baseSearchCriteria.getKey(), baseSearchCriteria.getOperation(), intVal, baseSearchCriteria.getSubstituteField(), baseSearchCriteria.getFieldType()));
                        } catch (Exception e) {
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
            // query.distinct(true); // @FIXME FOR WHAT ???
            // --------------------------------------------------------
            // Check for path like user.username
            String[] criteriaKeys = criteria.getKey().split("\\.");
            Path criteriaPath;
            if(criteriaKeys.length == 2) {
                Path criteriaPath_0 = root.get(criteriaKeys[0]);
                // --------------------------------------------------------
                String criteriaRelationType;
                if (criteriaPath_0.getClass() == SingularAttributePath.class) {
                    criteriaRelationType = ((SingularAttributePath) criteriaPath_0).getAttribute().getPersistentAttributeType().toString();
                } else if (criteriaPath_0.getClass() == PluralAttributePath.class){
                    criteriaRelationType = ((PluralAttributePath) criteriaPath_0).getAttribute().getPersistentAttributeType().toString();
                } else {
                    criteriaRelationType = "UNKNOWN";
                }
                // --------------------------------------------------------
                switch (criteriaRelationType){
                    case "MANY_TO_ONE":
                        criteriaPath = root.get(criteriaKeys[0]).get(criteriaKeys[1]);
                        break;
                    case "MANY_TO_MANY":
                        criteriaPath = root.join(criteriaKeys[0]).get(criteriaKeys[1]);
                        break;
                    default:
                        criteriaPath = root.get(criteriaKeys[0]);
                        break;
                }
            } else {
                criteriaPath = root.get(criteriaKeys[0]);
            }
            // --------------------------------------------------------
            // For LocalDateTime > or >= is 1 second different
            if (criteriaOperation.equalsIgnoreCase(">")) {
                if (criteriaPath.getJavaType() == LocalDateTime.class) {
                    // // End of day
                    LocalDateTime criteriaValue = LocalDateTime.from(((LocalDate) criteria.getValue()).atTime(LocalTime.MAX));
                    return criteriaBuilder.greaterThan( criteriaPath, criteriaValue );
                } else {
                    return criteriaBuilder.greaterThan( criteriaPath, criteria.getValue().toString() );
                }
            } else if (criteriaOperation.equalsIgnoreCase(">=")) {
                if (criteriaPath.getJavaType() == LocalDateTime.class) {
                    // Start of day
                    LocalDateTime criteriaValue = LocalDateTime.from(((LocalDate) criteria.getValue()).atStartOfDay());
                    return criteriaBuilder.greaterThanOrEqualTo( criteriaPath, criteriaValue );
                } else {
                    return criteriaBuilder.greaterThanOrEqualTo( criteriaPath, criteria.getValue().toString() );
                }
            } else if (criteriaOperation.equalsIgnoreCase("<")) {
                if (criteriaPath.getJavaType() == LocalDateTime.class) {
                    // Start of day
                    LocalDateTime criteriaValue = LocalDateTime.from(((LocalDate) criteria.getValue()).atStartOfDay());
                    return criteriaBuilder.lessThan( criteriaPath, criteriaValue );
                } else {
                    return criteriaBuilder.lessThan( criteriaPath, criteria.getValue().toString() );
                }
            } else if (criteriaOperation.equalsIgnoreCase("<=")) {
                if (criteriaPath.getJavaType() == LocalDateTime.class) {
                    // End of day
                    LocalDateTime criteriaValue = LocalDateTime.from(((LocalDate) criteria.getValue()).atTime(LocalTime.MAX));
                    // LocalDateTime criteriaValue = LocalDateTime.from(((LocalDate) criteria.getValue()).atStartOfDay());
                    return criteriaBuilder.lessThanOrEqualTo( criteriaPath, criteriaValue );
                } else {
                    return criteriaBuilder.lessThanOrEqualTo( criteriaPath, criteria.getValue().toString() );
                }
            } else if (criteriaOperation.equalsIgnoreCase(":")) {
                if (criteriaPath.getJavaType() == String.class) {
                    return criteriaBuilder.like(
                            criteriaBuilder.upper(criteriaPath),
                            "%" + ((String) criteria.getValue()).toUpperCase() + "%"
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
