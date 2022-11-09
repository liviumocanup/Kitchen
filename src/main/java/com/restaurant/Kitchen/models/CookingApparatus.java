package com.restaurant.Kitchen.models;

import com.restaurant.Kitchen.service.KitchenService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.restaurant.Kitchen.service.KitchenService.TIME_UNIT;

public abstract class CookingApparatus {
    private final BlockingQueue<Pair<Cook, Item>> items = new LinkedBlockingQueue<>();

    public CookingApparatus(int nrOfAppliances) {

        for (int i = 0; i < nrOfAppliances; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        Pair<Cook, Item> orderItemPair = items.take();
                        Item orderItem = orderItemPair.getRight();
                        Cook cook = orderItemPair.getLeft();
                        Thread.sleep(orderItem.getCookingTime() * TIME_UNIT);
                        KitchenService.checkIfOrderIsReady(orderItem, cook.getId());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    public void addOrderItemToQueue(Cook cook, Item orderItem) {
        items.add(Pair.of(cook, orderItem));
    }
}
