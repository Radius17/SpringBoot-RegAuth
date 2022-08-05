package ru.radius17.reg_auth.utils;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchSpecificationsBuilder {
    private final List<SearchCriteria> params;
    private final boolean orPredicate;

    public SearchSpecificationsBuilder(boolean orPredicate) {
        this.orPredicate = orPredicate;
        params = new ArrayList<>();
    }

    public SearchSpecificationsBuilder() {
        this.orPredicate = false;
        params = new ArrayList<>();
    }

    public SearchSpecificationsBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value, this.orPredicate));
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
            String criteriaKey = criteria.getKey();

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
                return criteriaBuilder.greaterThanOrEqualTo(
                        criteriaPath, criteria.getValue().toString());
            } else if (criteriaOperation.equalsIgnoreCase("<")) {
                return criteriaBuilder.lessThanOrEqualTo(
                        criteriaPath, criteria.getValue().toString());
            } else if (criteriaOperation.equalsIgnoreCase(":")) {
                if (criteriaPath.getJavaType() == String.class) {
                    return criteriaBuilder.like(
                            criteriaPath, "%" + criteria.getValue() + "%");
                } else {
                    return criteriaBuilder.equal(criteriaPath, criteria.getValue());
                }
            }
            return null;
        }
    }
}
