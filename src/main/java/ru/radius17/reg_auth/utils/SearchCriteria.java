package ru.radius17.reg_auth.utils;

import lombok.Getter;

@Getter
public class SearchCriteria {
    private final String key;
    private final String operation;
    private final Object value;
    private final boolean orPredicate;

    public SearchCriteria(String key, String operation, Object value, boolean orPredicate) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        this.orPredicate = orPredicate;
    }

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        this.orPredicate = false;
    }
}
