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

    private final PhoneRepository phoneRepository;

    public PhoneService(PhoneRepository phoneRepository) {
        this.phoneRepository = phoneRepository;
    }

    public List<PhoneResponseDto> findAll() {
        log.info("Fetching all phones");
        return phoneRepository.findAll()
                .stream()
                .map(OrderEventMapper::toPhoneResponseDto)
                .toList();
    }

    public Optional<PhoneResponseDto> findById(Long phoneId) {
        log.info("Fetching phone with id: {}", phoneId);
        return phoneRepository.findById(phoneId)
                .map(OrderEventMapper::toPhoneResponseDto);
    }

    @Transactional
    public PhoneResponseDto create(PhoneDto phoneDto) {
        log.info("Creating phone: {}", phoneDto);
        Phone phone = OrderEventMapper.toPhoneEntity(phoneDto);
        Phone savedPhone = phoneRepository.save(phone);
        log.info("Successfully created phone: {}", savedPhone);
        return OrderEventMapper.toPhoneResponseDto(savedPhone);
    }

    @Transactional
    public Optional<PhoneResponseDto> update(Long orderId, PhoneDto phoneDto) {
        log.info("Updating phone with id: {}", orderId);
        return phoneRepository.findById(orderId)
                .map(existingPhone -> {
                    existingPhone.setPhoneName(phoneDto.phoneName());
                    existingPhone.setPhoneModel(phoneDto.phoneModel());
                    existingPhone.setPhonePrice(phoneDto.phonePrice());
                    existingPhone.setPhoneManufacturer(phoneDto.phoneManufacturer());
                    Phone updatedPhone = phoneRepository.save(existingPhone);
                    log.info("Successfully updated phone: {}", updatedPhone);
                    return OrderEventMapper.toPhoneResponseDto(updatedPhone);
                });
    }

    @Transactional
    public boolean delete(Long orderId) {
        log.info("Deleting order with id: {}", orderId);
        return phoneRepository.findById(orderId)
                .map(phone -> {
                    // Break the bidirectional OneToOne reference so that
                    // OrderEvent's cascade = ALL does not re-persist the phone
                    if (phone.getOrderEvent() != null) {
                        phone.getOrderEvent().setPhone(null);
                    }
                    phoneRepository.delete(phone);
                    log.info("Successfully deleted phone with id: {}", orderId);
                    return true;
                })
                .orElse(false);
    }
}


