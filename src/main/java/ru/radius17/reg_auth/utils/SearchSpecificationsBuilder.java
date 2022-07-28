package ru.radius17.reg_auth.utils;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
            if (criteria.getOperation().equalsIgnoreCase(">")) {
                return criteriaBuilder.greaterThanOrEqualTo(
                        root.get(criteria.getKey()), criteria.getValue().toString());
            } else if (criteria.getOperation().equalsIgnoreCase("<")) {
                return criteriaBuilder.lessThanOrEqualTo(
                        root.get(criteria.getKey()), criteria.getValue().toString());
            } else if (criteria.getOperation().equalsIgnoreCase(":")) {
                if (root.get(criteria.getKey()).getJavaType() == String.class) {
                    return criteriaBuilder.like(
                            root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
                } else {
                    return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
                }
            }
            return null;
        }
    }
}
