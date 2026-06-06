package com.kodebytes.acasado.config;

public final class AppConstants {

    // Utility class make sure it cannot be instantiated
    private AppConstants() { }

    public static final String API_BASE_PATH = "/api/orderevent";
    public static final String DEFAULT_ORDER_EVENTS_TOPIC = "order-events";
    public static final String DEFAULT_PRODUCER_ACKS = "all";
}

