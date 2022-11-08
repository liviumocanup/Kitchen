package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @JsonAlias("order_id")
    private Integer orderId;

    @JsonAlias("table_id")
    private Integer tableId;

    @JsonAlias("waiter_id")
    private Integer waiterId;

    @JsonAlias("items")
    private List<Integer> items;

    @JsonAlias("priority")
    private Integer priority;

    @JsonAlias("max_wait")
    private Double maxWait;

    @JsonAlias("pick_up_time")
    private Long pickUpTime;

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