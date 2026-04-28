package com.kodebytes.acasado.controller;

import com.kodebytes.acasado.controller.OrderEventsControllerAdvice.ErrorResponse;
import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.domain.OrderEventType;
import com.kodebytes.acasado.service.OrderEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orderevent")
public class OrderEventsController {

    private final OrderEventService orderEventService;

    public OrderEventsController(OrderEventService orderEventService) {
        this.orderEventService = orderEventService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<?>> createOrderEvent(
            @RequestBody @Valid OrderEvent orderEvent) {

        if (orderEvent.eventType() != OrderEventType.ADD) {
            return CompletableFuture.completedFuture(
                    ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse(List.of("only ADD event type is supported"))));
        }

        return orderEventService.createOrderEvent(orderEvent)
                .thenApply(created -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(created));
    }

    @PutMapping
    public CompletableFuture<ResponseEntity<?>> updateOrderEvent(
            @RequestBody @Valid OrderEvent orderEvent) {

        if (orderEvent.orderId() == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse(List.of("orderEventId is required for UPDATE"))));
        }

        if (orderEvent.eventType() != OrderEventType.UPDATE) {
            return CompletableFuture.completedFuture(
                    ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse(List.of("only UPDATE event type is supported"))));
        }

        return orderEventService.updateOrderEvent(orderEvent)
                .thenApply(updated -> ResponseEntity.ok().body(updated));
    }
}

