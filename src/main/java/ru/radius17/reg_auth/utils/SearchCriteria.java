package ru.radius17.reg_auth.utils;

import lombok.Getter;

@Getter
public class SearchCriteria {
    private final String key;
    private final String substituteField;
    private final String fieldType;
    private final String operation;
    private final Object value;
    private final boolean orPredicate;

    public SearchCriteria(String key, String operation, Object value, String substituteField, String fieldType) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        if(substituteField == null || substituteField.isEmpty()) this.substituteField = key;
        else this.substituteField = substituteField;
        this.fieldType = fieldType;

        this.orPredicate = false;
    }
    public SearchCriteria(String key, String operation, Object value, String substituteField, String fieldType, boolean orPredicate) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        if(substituteField == null || substituteField.isEmpty()) this.substituteField = key;
        else this.substituteField = substituteField;
        this.fieldType = fieldType;

        this.orPredicate = orPredicate;
    }
}
