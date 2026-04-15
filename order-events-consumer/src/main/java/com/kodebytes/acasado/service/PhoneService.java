package com.kodebytes.acasado.service;

import com.kodebytes.acasado.domain.OrderEventMapper;
import com.kodebytes.acasado.domain.Phone;
import com.kodebytes.acasado.dto.PhoneDto;
import com.kodebytes.acasado.dto.PhoneResponseDto;
import com.kodebytes.acasado.repository.PhoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PhoneService {

    private static final Logger log = LoggerFactory.getLogger(PhoneService.class);

    private final PhoneRepository bookRepository;

    public PhoneService(PhoneRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<PhoneResponseDto> findAll() {
        log.info("Fetching all books");
        return bookRepository.findAll()
                .stream()
                .map(OrderEventMapper::toPhoneResponseDto)
                .toList();
    }

    public Optional<PhoneResponseDto> findById(Long bookId) {
        log.info("Fetching book with id: {}", bookId);
        return bookRepository.findById(bookId)
                .map(OrderEventMapper::toPhoneResponseDto);
    }

    @Transactional
    public PhoneResponseDto create(PhoneDto phoneDto) {
        log.info("Creating book: {}", phoneDto);
        Phone book = OrderEventMapper.toPhoneEntity(phoneDto);
        Phone savedPhone = bookRepository.save(book);
        log.info("Successfully created book: {}", savedPhone);
        return OrderEventMapper.toPhoneResponseDto(savedPhone);
    }

    @Transactional
    public Optional<PhoneResponseDto> update(Long orderId, PhoneDto phoneDto) {
        log.info("Updating phone with id: {}", orderId);
        return bookRepository.findById(orderId)
                .map(existingPhone -> {
                    existingPhone.setPhoneName(phoneDto.phoneName());
                    existingPhone.setPhoneModel(phoneDto.phoneModel());
                    existingPhone.setPhonePrice(phoneDto.phonePrice());
                    existingPhone.setPhoneManufacturer(phoneDto.phoneManufacturer());
                    Phone updatedPhone = bookRepository.save(existingPhone);
                    log.info("Successfully updated book: {}", updatedPhone);
                    return OrderEventMapper.toPhoneResponseDto(updatedPhone);
                });
    }

    @Transactional
    public boolean delete(Long orderId) {
        log.info("Deleting order with id: {}", orderId);
        return bookRepository.findById(orderId)
                .map(phone -> {
                    // Break the bidirectional OneToOne reference so that
                    // LibraryEvent's cascade = ALL does not re-persist the book
                    if (phone.getOrderEvent() != null) {
                        phone.getOrderEvent().setPhone(null);
                    }
                    bookRepository.delete(phone);
                    log.info("Successfully deleted book with id: {}", orderId);
                    return true;
                })
                .orElse(false);
    }
}


