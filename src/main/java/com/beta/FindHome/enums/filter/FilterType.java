package com.beta.FindHome.enums.filter;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum FilterType {
    FLAT,
    HOUSE,
    ROOM,
    ALL;

    @JsonCreator
    public static FilterType fromString(String value) {
        return FilterType.valueOf(value.toUpperCase());
    }
}