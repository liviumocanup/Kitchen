package com.restaurant.Kitchen.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.restaurant.Kitchen.constants.CookingApparatus;
import com.restaurant.Kitchen.service.KitchenService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.restaurant.Kitchen.service.KitchenService.items;

@Getter
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
    private ExecutorService executorService;

    @JsonIgnore
    private static final int TIME_UNIT = KitchenService.TIME_UNIT;

    @JsonIgnore
    private static final List<Item> checkedFoods = new CopyOnWriteArrayList<>();

    @JsonIgnore
    private AtomicInteger concurrentDishesCounter = new AtomicInteger();

    private void lookForFoodToPrepare() {
        executorService = Executors.newFixedThreadPool(proficiency);

        Runnable checkItems = () -> {

            while (true) {
                if(concurrentDishesCounter.get()<proficiency){
                    Item item = findRightFoodToCook();

                    itemIsCooked(item);

                    KitchenService.checkIfOrderIsReady(item, id);
                }else {
                    throw new RuntimeException();
                }
            }

        };

        for (int i = 0; i < proficiency; i++) {
            executorService.execute(checkItems);
        }
    }

    private Item findRightFoodToCook() {
        try {
            Optional<Item> item;
            do {
                item = items.stream().filter(i -> i.getComplexity() <= rank).findFirst();
                if(item.isEmpty())
                    Thread.sleep(TIME_UNIT);
            } while(item.isEmpty());

            items.remove(item.get());
            concurrentDishesCounter.incrementAndGet();
            return item.get();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void itemIsCooked(Item item) {
        try {
            if (item.getCookingApparatus() != null) {
                Semaphore semaphore = item.getCookingApparatus().equals(CookingApparatus.OVEN) ?
                        KitchenService.ovenSemaphore : KitchenService.stoveSemaphore;

                semaphore.acquire();
                Thread.sleep(item.getCookingTime() * TIME_UNIT);
                concurrentDishesCounter.decrementAndGet();
                semaphore.release();
            } else {
                Thread.sleep(item.getCookingTime() * TIME_UNIT);
                concurrentDishesCounter.decrementAndGet();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCatchPhrase(String catchPhrase) {
        this.catchPhrase = catchPhrase;
        lookForFoodToPrepare();
    }

    public void setProficiency(int proficiency) {
        this.proficiency = proficiency;
    }
}