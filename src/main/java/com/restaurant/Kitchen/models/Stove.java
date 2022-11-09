package com.restaurant.Kitchen.models;

public class Stove extends CookingApparatus {
    public static final Integer NUMBER_OF_STOVES = 1;

    private static final Stove instance = new Stove(NUMBER_OF_STOVES);

    public static Stove getInstance() {
        return instance;
    }

    private Stove(int nrOfOvens) {
        super(nrOfOvens);
    }
}
