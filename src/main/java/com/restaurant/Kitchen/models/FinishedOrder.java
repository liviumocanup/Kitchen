package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FinishedOrder {
    @JsonAlias("order_id")
    private int orderId;

    @JsonAlias("table_id")
    private int tableId;

    @JsonAlias("waiter_id")
    private int waiterId;

    @JsonAlias("items")
    private List<Integer> items = new ArrayList<>();

    @JsonAlias("priority")
    private int priority;

    @JsonAlias("max_wait")
    private double maxWait;

    @JsonAlias("pick_up_time")
    private long pickUpTime;

    @JsonAlias("cooking_time")
    private long cookingTime;

    @JsonAlias("cooking_details")
    private List<CookingDetails> cookingDetails;

    public FinishedOrder(Order order, Long cookingTime, List<CookingDetails> cookingDetails) {
        this.orderId = order.getOrderId();
        this.tableId = order.getTableId();
        this.waiterId = order.getWaiterId();
        this.items = order.getItems();
        this.priority = order.getPriority();
        this.maxWait = order.getMaxWait();
        this.pickUpTime = order.getPickUpTime();

        this.cookingTime = cookingTime;
        this.cookingDetails = cookingDetails;
    }

    @Override
    public String toString() {
        return "FinishedOrder{" +
                "orderId=" + orderId +
                ", tableId=" + tableId +
                ", waiterId=" + waiterId +
                ", items=" + items +
                ", priority=" + priority +
                ", maxWait=" + maxWait +
                ", pickUpTime=" + pickUpTime +
                ", cookingTime=" + cookingTime +
                ", cookingDetails=" + cookingDetails +
                '}';
    }
}