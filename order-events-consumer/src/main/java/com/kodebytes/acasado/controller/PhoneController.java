package com.kodebytes.acasado.controller;

import com.kodebytes.acasado.dto.PhoneDto;
import com.kodebytes.acasado.dto.PhoneResponseDto;
import com.kodebytes.acasado.service.PhoneService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phones")
public class PhoneController {

    private static final Logger log = LoggerFactory.getLogger(PhoneController.class);

    private final PhoneService phoneService;

    public PhoneController(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @GetMapping
    public ResponseEntity<List<PhoneResponseDto>> getAllPhones() {
        log.info("GET /v1/books");
        List<PhoneResponseDto> books = phoneService.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<PhoneResponseDto> getPhoneById(@PathVariable Long bookId) {
        log.info("GET /v1/books/{}", bookId);
        return phoneService.findById(bookId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PhoneResponseDto> createPhone(@RequestBody @Valid PhoneDto phoneDto) {
        log.info("POST /v1/books - {}", phoneDto);
        PhoneResponseDto created = phoneService.create(phoneDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<PhoneResponseDto> updatePhone(@PathVariable Long bookId,
                                                      @RequestBody @Valid PhoneDto bookDto) {
        log.info("PUT /v1/books/{} - {}", bookId, bookDto);
        return phoneService.update(bookId, bookDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long bookId) {
        log.info("DELETE /v1/books/{}", bookId);
        if (phoneService.delete(bookId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

