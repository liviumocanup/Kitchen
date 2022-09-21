package com.restaurant.Kitchen.controllers;

import com.restaurant.Kitchen.models.Order;
import com.restaurant.Kitchen.service.KitchenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class KitchenController {
    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void receiveOrder(@RequestBody Order order) {
        kitchenService.takeOrderToCook(order);
    }
}
