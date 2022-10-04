package com.restaurant.Kitchen.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.Kitchen.models.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;


@Service
public class KitchenService {
    static RestTemplate restTemplate = new RestTemplate();

    private static final List<Food> foodList = loadDefaultMenu();
    private List<Cook> cooks = Collections.synchronizedList(new ArrayList<>());
    public final Integer TIME_UNIT = 50;

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    public KitchenService() throws FileNotFoundException {
        cooks.add(new Cook(1, 2, "Ahmad Punjabi", "Hello, cousin."));
        cooks.add(new Cook(2, 2, "Valeriu Moraru", "Great Britain is the capital of London."));
        cooks.add(new Cook(3, 3, "Gordon Ramsay", "Hey, panini head, are you listening to me?"));
    }

    public void takeOrderToCook(Order order) {

        Runnable runnableTask = () -> {
            for (Cook cook : cooks) {
                if (!cook.isBusy().compareAndExchange(false, true)){
                    restTemplate.postForObject("http://dining-hall-docker:8080/distribution", cook.prepareOrder(order, foodList, TIME_UNIT), String.class);
                    break;
                }
            }
        };

        executorService.execute(runnableTask);


    }

    private static List<Food> loadDefaultMenu(){
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = KitchenService.class.getResourceAsStream("/menu-items.json");
        try {
            return mapper.readValue(is, new TypeReference<List<Food>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
