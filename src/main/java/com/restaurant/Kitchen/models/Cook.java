package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.restaurant.Kitchen.service.KitchenService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Slf4j
public class Cook {
    @JsonAlias("id")
    private int id;

    @JsonAlias("rank")
    private int rank;

    @JsonAlias("proficiency")
    private int proficiency;

    @JsonAlias("name")
    private String name;

    @JsonAlias("catch-phrase")
    private String catchPhrase;

    @JsonIgnore
    private AtomicInteger concurrentDishesCounter = new AtomicInteger();

    @JsonIgnore
    private AtomicBoolean full = new AtomicBoolean(false);

    @JsonIgnore
    private ExecutorService executorService;

    @JsonIgnore
    private static final int TIME_UNIT = KitchenService.TIME_UNIT;

    public void prepareItem(Item item) {
        if (concurrentDishesCounter.compareAndSet(proficiency, concurrentDishesCounter.intValue())) {
            full.set(true);
        }
        this.concurrentDishesCounter.getAndIncrement();
        executorService.execute(() -> cookItem(item));
    }

    public void cookItem(Item item) {
        try {
            Thread.sleep(item.getCookingTime() * TIME_UNIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.concurrentDishesCounter.getAndDecrement();
        log.info("+ Order " + item + " is ready.");
        full.set(false);
        KitchenService.checkIfOrderIsReady(item, this.id);
    }

    public int getConcurrentDishesCounter() {
        return concurrentDishesCounter.intValue();
    }

    @Override
    public String toString() {
        return "Cook{" +
                "id=" + id +
                ", rank=" + rank +
                ", proficiency=" + proficiency +
                ", name='" + name + '\'' +
                ", catchPhrase='" + catchPhrase + '\'' +
                ", executorService=" + executorService +
                ", concurrentDishesCounter=" + concurrentDishesCounter +
                '}';
    }

    public void setProficiency(int proficiency) {
        this.proficiency = proficiency;
        executorService = Executors.newFixedThreadPool(proficiency);
    }
}