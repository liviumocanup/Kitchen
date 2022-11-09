package com.restaurant.Kitchen.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.Kitchen.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.restaurant.Kitchen.models.Oven.NUMBER_OF_OVENS;
import static com.restaurant.Kitchen.models.Stove.NUMBER_OF_STOVES;

@Service
@Slf4j
public class KitchenService {
    public static final Map<Integer, Food> foodList = new HashMap<>();
    public static final List<Cook> cookList = loadDefaultCooks();
    public static final Map<Integer, Order> orderList = new HashMap<>();

    public static final Map<Integer, List<CookingDetails>> orderToFoodListMap = new ConcurrentHashMap<>();

    public static BlockingQueue<Item> items = new LinkedBlockingQueue<>();

    public final static int TIME_UNIT = 50;

    private static String DINING_HALL_URL;

    private static final Map<Integer, List<Cook>> rankMap = new HashMap<>();

    @Value("${dining-hall-service.url}")
    public void setDiningHallServiceUrl(String url) {
        DINING_HALL_URL = url;
    }

    @Value("${restaurant.menu}")
    public String restaurantMenu;

    @PostConstruct
    public void init() {
        System.out.println(restaurantMenu);
        loadDefaultMenu();

        for (int i = 1; i <= 3; i++) {
            rankMap.put(i, new ArrayList<>());
            for (Cook cook : cookList) {
                if (cook.getRank() >= i) {
                    rankMap.get(i).add(cook);
                }
            }
        }
    }

    public void receiveOrder(Order order) {
        order.setReceivedAt(Instant.now());
        log.info("-> Received " + order + " successfully.");

        orderToFoodListMap.put(order.getOrderId(), new ArrayList<>());
        orderList.put(order.getOrderId(), order);

        List<Item> orderItems = new CopyOnWriteArrayList<>();
        for (Integer foodId : order.getItems()) {
            Food currentFood = getItemById(foodId);
            orderItems.add(new Item(currentFood.getId(), order.getOrderId(), order.getPriority(), currentFood.getCookingApparatusType(),
                    currentFood.getPreparationTime(), currentFood.getComplexity()));
        }

        orderItems.stream()
                .sorted(Comparator.comparingInt(Item::getPriority))
                .forEach(item -> items.add(item));
    }

    public static synchronized void checkIfOrderIsReady(Item item, int cookId) {

        Order order = orderList.get(item.getOrderId());

        if (order != null) {
            List<CookingDetails> cookingDetails = orderToFoodListMap.get(item.getOrderId());
            cookingDetails.add(new CookingDetails(item.getMenuId(), cookId));
            if (cookingDetails.size() == order.getItems().size()) {
                sendFinishedOrderBackToKitchen(order);
            }
        }
    }

    private static void sendFinishedOrderBackToKitchen(Order order) {
        orderList.remove(order.getOrderId());

        Long cookingTime = (Instant.now().getEpochSecond() - order.getReceivedAt().getEpochSecond());
        FinishedOrder finishedOrder = new FinishedOrder(order, cookingTime, orderToFoodListMap.get(order.getOrderId()));

        orderToFoodListMap.remove(order.getOrderId());

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Void> response = restTemplate.postForEntity(DINING_HALL_URL, finishedOrder, Void.class);
        if (response.getStatusCode() != HttpStatus.ACCEPTED) {
            log.error("<!!!!!> " + order + " was unsuccessful (couldn't be sent back to dining hall service).");
        } else {
            log.info("<- " + finishedOrder + " was sent back to Dining Hall successfully.");
        }
    }

    public Double getEstimatedPrepTimeForOrderById(Integer orderId) {
        List<CookingDetails> foodDetails = orderToFoodListMap.get(orderId);
        Order order = orderList.get(orderId);

        if (foodDetails != null && order != null) {
            int B = cookList.stream().mapToInt(Cook::getProficiency).sum();

            List<Integer> cookedItemsIds = orderToFoodListMap.get(orderId).stream()
                    .map(CookingDetails::getFoodId)
                    .collect(Collectors.toList());

            List<Food> itemsNotReady = order.getItems().stream()
                    .filter(i -> !cookedItemsIds.contains(i))
                    .map(this::getItemById)
                    .collect(Collectors.toList());

            double A = 0, C = 0;

            if (itemsNotReady.isEmpty())
                return 0D;

            for (Food item : itemsNotReady) {
                if (item.getCookingApparatusType() == null) {
                    A += item.getPreparationTime();
                } else {
                    C += item.getPreparationTime();
                }
            }

            int D = NUMBER_OF_OVENS + NUMBER_OF_STOVES;

            double E = items.size();
            int F = itemsNotReady.size();

            return (A / B + C / D) * (E + F) / F;
        } else return 0D;
    }

    private Food getItemById(Integer id) {
        return foodList.get(id);
    }

    private void loadDefaultMenu() {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(restaurantMenu);
        InputStream is = KitchenService.class.getResourceAsStream("/" + restaurantMenu);
        try {
            List<Food> f = mapper.readValue(is, new TypeReference<>() {
            });
            for (Food food : f) {
                foodList.put(food.getId(), food);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Cook> loadDefaultCooks() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = KitchenService.class.getResourceAsStream("/cooks.json");
        try {
            return mapper.readValue(is, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
