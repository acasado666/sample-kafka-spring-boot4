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
@RequestMapping("/api/orders")
public class LibraryEventController {

    private static final Logger log = LoggerFactory.getLogger(LibraryEventController.class);

    private final OrderEventService orderEventService;

    public LibraryEventController(OrderEventService orderEventService) {
        this.orderEventService = orderEventService;
    }

    @GetMapping
    public ResponseEntity<List<OrderEventResponseDto>> getAllLibraryEvents() {
        log.info("GET /v1/library-events");
        List<OrderEventResponseDto> libraryEvents = orderEventService.findAll();
        return ResponseEntity.ok(libraryEvents);
    }

    @GetMapping("/{libraryEventId}")
    public ResponseEntity<OrderEventResponseDto> getLibraryEventById(
            @PathVariable Long libraryEventId) {
        log.info("GET /v1/library-events/{}", libraryEventId);
        return orderEventService.findById(libraryEventId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

