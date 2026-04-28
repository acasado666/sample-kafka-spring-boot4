package com.kodebytes.acasado.controller;

import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.service.OrderEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order-events")
public class OrderEventController {

    private static final Logger log = LoggerFactory.getLogger(OrderEventController.class);

    private final OrderEventService orderEventService;

    public OrderEventController(OrderEventService orderEventService) {
        this.orderEventService = orderEventService;
    }

    @GetMapping
    public ResponseEntity<List<OrderEventResponseDto>> getAllOrderEvents() {
        log.info("GET /api/order-events");
        List<OrderEventResponseDto> orderEvents = orderEventService.findAll();
        return ResponseEntity.ok(orderEvents);
    }

    @GetMapping("/{orderEventId}")
    public ResponseEntity<OrderEventResponseDto> getOrderEventById(
            @PathVariable Long orderEventId) {
        log.info("GET /api/order-events/{}", orderEventId);
        return orderEventService.findById(orderEventId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

