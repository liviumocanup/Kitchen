package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
public class CookingDetails {
    @JsonAlias("food_id")
    private int foodId;

    @JsonAlias("cook_id")
    private int cookId;

    public CookingDetails(int foodId, int cookId) {
        this.foodId = foodId;
        this.cookId = cookId;
    }

    @Override
    public String toString() {
        return "CookingDetails{" +
                "foodId=" + foodId +
                ", cookId=" + cookId +
                '}';
    }

    public CookingDetails() {
    }
}
