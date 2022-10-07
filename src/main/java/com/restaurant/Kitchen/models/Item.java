package com.restaurant.Kitchen.models;

import com.restaurant.Kitchen.constants.CookingApparatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    private int menuId;
    private int orderId;
    private int priority;
    private CookingApparatus cookingApparatus;
    private long cookingTime;
    private int complexity;

    public Item(int menuId, int orderId, int priority, CookingApparatus cookingApparatus, long cookingTime, int complexity) {
        this.menuId = menuId;
        this.orderId = orderId;
        this.priority = priority;
        this.cookingApparatus = cookingApparatus;
        this.cookingTime = cookingTime;
        this.complexity = complexity;
    }

    @Override
    public String toString() {
        return "Item{" +
                "menuId=" + menuId +
                ", orderId=" + orderId +
                ", priority=" + priority +
                ", cookingApparatus=" + cookingApparatus +
                ", cookingTime=" + cookingTime +
                ", complexity=" + complexity +
                '}';
    }
}
