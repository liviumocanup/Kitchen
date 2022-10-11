package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @JsonAlias("order_id")
    private int orderId;

    @JsonAlias("table_id")
    private int tableId;

    @JsonAlias("waiter_id")
    private int waiterId;

    @JsonAlias("items")
    private List<Integer> items;

    @JsonAlias("priority")
    private int priority;

    @JsonAlias("max_wait")
    private double maxWait;

    @JsonAlias("pick_up_time")
    private long pickUpTime;

    @JsonIgnore
    private Instant receivedAt;

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", tableId=" + tableId +
                ", waiterId=" + waiterId +
                ", items=" + items +
                ", priority=" + priority +
                ", maxWait=" + maxWait +
                ", pickUpTime=" + pickUpTime +
                ", receivedAt=" + receivedAt +
                '}';
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}