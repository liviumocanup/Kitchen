package com.restaurant.Kitchen.constants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CookingApparatusType {

    STOVE, OVEN;

    @JsonValue
    public String toLowerCase() {
        return toString().toLowerCase();
    }
}
