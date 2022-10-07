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
    private static final List<Food> foodList = loadDefaultMenu();
    private static final List<Cook> cookList = loadDefaultCooks();
    public static final List<Order> orderList = new CopyOnWriteArrayList<>();

    public static final Map<Integer, List<CookingDetails>> orderToFoodListMap = new ConcurrentHashMap<>();

    private final ExecutorService orderItemDispatcher = Executors.newSingleThreadExecutor();


    public final static int TIME_UNIT = 500;

    public void receiveOrder(Order order) {
        order.setReceivedAt(Instant.now());
        orderList.add(order);

        List<Item> orderItems = new CopyOnWriteArrayList<>();
        for (Integer foodId : order.getItems()) {
            Food currentFood = foodList.get(foodId - 1);
            orderItems.add(new Item(foodId, order.getOrderId(), order.getPriority(), currentFood.getCookingApparatus(),
                    currentFood.getPreparationTime(), currentFood.getComplexity()));
        }

        orderItems.stream().sorted(Comparator.comparingInt(Item::getPriority)).forEach(item -> orderItemDispatcher.submit(() -> findRightCook(item)));
    }

    private void findRightCook(Item item) {
        Optional<Cook> selectedCook = cookList.stream()
                .filter(c -> c.getRank() >= item.getComplexity())
                .filter(c -> !c.getFull().get())
                .min(Comparator.comparing(Cook::getConcurrentDishesCounter)
                        .thenComparing(Cook::getRank))
                .stream().findFirst();
        if(selectedCook.isEmpty()) {
            try {
                Thread.sleep(2 * TIME_UNIT);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            findRightCook(item);
        }
        else selectedCook.get().prepareItem(item);
    }

    public synchronized static void checkIfOrderIsReady(Item item, int cookId) {

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
            log.error("Order couldn't be sent back to dinning hall service!");
        } else {
            log.info("<-- "+finishedOrder+" was sent back to kitchen successfully.");
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
