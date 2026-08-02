package com.kodebytes.acasado.controller;

import com.kodebytes.acasado.dto.PhoneRequestDto;
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
        log.info("GET /api/phones");
        List<PhoneResponseDto> books = phoneService.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{phoneId}")
    public ResponseEntity<PhoneResponseDto> getPhoneById(@PathVariable Integer phoneId) {
        log.info("GET /api/phones/{}", phoneId);
        return phoneService.findById(phoneId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PhoneResponseDto> createPhone(@RequestBody @Valid PhoneRequestDto phoneRequestDto) {
        log.info("POST /api/phones - {}", phoneRequestDto);
        PhoneResponseDto created = phoneService.create(phoneRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{phoneId}")
    public ResponseEntity<PhoneResponseDto> updatePhone(@PathVariable Integer phoneId,
                                                        @RequestBody @Valid PhoneRequestDto phoneRequestDto) {
        log.info("PUT /api/phones - {} - {}", phoneId, phoneRequestDto);
        return phoneService.update(phoneId, phoneRequestDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{phoneId}")
    public ResponseEntity<Void> deletePhone(@PathVariable Integer phoneId) {
        log.info("DELETE /v1/books/{}", phoneId);
        if (phoneService.delete(phoneId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

