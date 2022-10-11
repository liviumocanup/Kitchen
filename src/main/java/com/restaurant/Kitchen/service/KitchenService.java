package com.restaurant.Kitchen.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.Kitchen.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class KitchenService {
    public static final List<Food> foodList = loadDefaultMenu();
    public static final List<Cook> cookList = loadDefaultCooks();
    public static final List<Order> orderList = new CopyOnWriteArrayList<>();

    public static final Map<Integer, List<CookingDetails>> orderToFoodListMap = new ConcurrentHashMap<>();

    private static final Integer NUMBER_OF_STOVES = 1;
    private static final Integer NUMBER_OF_OVENS = 2;

    public static final Semaphore stoveSemaphore = new Semaphore(NUMBER_OF_STOVES);
    public static final Semaphore ovenSemaphore = new Semaphore(NUMBER_OF_OVENS);

    public static BlockingQueue<Item> items = new LinkedBlockingQueue<>();

    public final static int TIME_UNIT = 50;

    public void receiveOrder(Order order) {
        order.setReceivedAt(Instant.now());
        log.info("--> Received " + order + " successfully.");
        orderList.add(order);

        List<Item> orderItems = new CopyOnWriteArrayList<>();
        for (Integer foodId : order.getItems()) {
            Food currentFood = foodList.get(foodId - 1);
            orderItems.add(new Item(foodId, order.getOrderId(), order.getPriority(), currentFood.getCookingApparatus(),
                    currentFood.getPreparationTime(), currentFood.getComplexity()));
        }

        orderItems.stream()
                .sorted(Comparator.comparingInt(Item::getPriority))
                .forEach(item -> items.add(item));
    }

    public static synchronized void checkIfOrderIsReady(Item item, int cookId) {

        Order order = orderList.stream()
                .filter(order1 -> order1.getOrderId() == item.getOrderId())
                .findFirst()
                .orElseThrow();

        orderToFoodListMap.putIfAbsent(item.getOrderId(), new ArrayList<>());
        List<CookingDetails> cookingDetails = orderToFoodListMap.get(item.getOrderId());
        cookingDetails.add(new CookingDetails(item.getMenuId(), cookId));
        if (cookingDetails.size() == order.getItems().size()) {
            sendFinishedOrderBackToKitchen(order);
        }
    }

    private static void sendFinishedOrderBackToKitchen(Order order) {
        Long cookingTime = (Instant.now().getEpochSecond() - order.getReceivedAt().getEpochSecond());
        FinishedOrder finishedOrder = new FinishedOrder(order, cookingTime, orderToFoodListMap.get(order.getOrderId()));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> response = restTemplate.postForEntity("http://localhost:8080/distribution", finishedOrder, Void.class);
        if (response.getStatusCode() != HttpStatus.ACCEPTED) {
            log.error("<!!!!!> " + order + " was unsuccessful (couldn't be sent back to dinning hall service).");
        } else {
            log.info("<-- "+finishedOrder+" was sent back to Kitchen successfully.");
        }
    }

    private static List<Food> loadDefaultMenu() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = KitchenService.class.getResourceAsStream("/menu-items.json");
        try {
            return mapper.readValue(is, new TypeReference<List<Food>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Cook> loadDefaultCooks() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = KitchenService.class.getResourceAsStream("/cooks.json");
        try {
            return mapper.readValue(is, new TypeReference<List<Cook>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
