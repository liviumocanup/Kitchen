package com.restaurant.Kitchen.models;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class Cook {
    private Long id;
    private int rank;
    private int proficiency;
    private String name;
    private String catchPhrase;

    public AtomicBoolean isBusy() {
        return busy;
    }

    private AtomicBoolean busy;

    private AtomicLong idCounter = new AtomicLong();

    @Override
    public String toString() {
        return "Cook{" +
                "rank=" + rank +
                ", proficiency=" + proficiency +
                ", name='" + name + '\'' +
                ", catchPhrase='" + catchPhrase + '\'' +
                '}';
    }

    public Cook(int rank, int proficiency, String name, String catchPhrase) {
        this.id = idCounter.incrementAndGet();
        this.rank = rank;
        this.proficiency = proficiency;
        this.name = name;
        this.catchPhrase = catchPhrase;
        this.busy = new AtomicBoolean(false);
    }

    public FinishedOrder prepareOrder(Order order, List<Food> foodList,int TIME_UNIT){
        System.out.println("+ Cook "+this.name+" received " + order.toString());
        busy.set(true);

        CookingDetails cookingDetails = new CookingDetails();
        Long[] items = order.getItems();

        long startTime = System.nanoTime();

        for (Long item : items) {
            try {
                cookingDetails.addCookingDetail(new CookingDetail(item, this.id));
                for(Food food : foodList){
                    if (food.getId().equals(item))
                        sleep(food.getPreparation_time()*TIME_UNIT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();

        FinishedOrder finishedOrder = new FinishedOrder(order.getOrder_id(), order.getTable_id(), order.getWaiter_id(), order.getItems(), order.getPriority(),
                order.getMax_wait(), order.getPick_up_time(), (double) (endTime - startTime), cookingDetails);

        System.out.println(this.name + " " + finishedOrder);
        busy.set(false);
        return finishedOrder;
    }
}
