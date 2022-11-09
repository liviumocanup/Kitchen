package com.restaurant.Kitchen.models;

import com.restaurant.Kitchen.constants.CookingApparatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    private int menuId;
    private int orderId;
    private int priority;
    private CookingApparatusType cookingApparatusType;
    private long cookingTime;
    private int complexity;

    public Item(int menuId, int orderId, int priority, CookingApparatusType cookingApparatusType, long cookingTime, int complexity) {
        this.menuId = menuId;
        this.orderId = orderId;
        this.priority = priority;
        this.cookingApparatusType = cookingApparatusType;
        this.cookingTime = cookingTime;
        this.complexity = complexity;
    }

    @Override
    public String toString() {
        return "Item{" +
                "menuId=" + menuId +
                ", orderId=" + orderId +
                ", priority=" + priority +
                ", cookingApparatus=" + cookingApparatusType +
                ", cookingTime=" + cookingTime +
                ", complexity=" + complexity +
                '}';
    }
}
