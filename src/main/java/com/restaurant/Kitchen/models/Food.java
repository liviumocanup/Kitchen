package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.restaurant.Kitchen.constants.CookingApparatusType;
import lombok.Getter;

@Getter
public class Food {
    @JsonAlias("id")
    private int id;

    @JsonAlias("name")
    private String name;

    @JsonAlias("preparation-time")
    private Long preparationTime;

    @JsonAlias("complexity")
    private int complexity;

    @JsonAlias("cooking-apparatus")
    private CookingApparatusType cookingApparatusType;

    @Override
    public String toString() {
        return "Food{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", preparationTime=" + preparationTime +
                ", complexity=" + complexity +
                ", cookingApparatus=" + cookingApparatusType +
                '}';
    }
}