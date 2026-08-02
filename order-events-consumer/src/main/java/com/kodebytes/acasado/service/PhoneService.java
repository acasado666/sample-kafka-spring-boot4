package com.kodebytes.acasado.service;

import com.kodebytes.acasado.dto.PhoneRequestDto;
import com.kodebytes.acasado.dto.PhoneResponseDto;
import com.kodebytes.acasado.entity.PhoneDao;
import com.kodebytes.acasado.entity.OrderEventDao;
import com.kodebytes.acasado.mapper.OrderEventMapper;
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
    private final OrderEventMapper orderEventMapper;

    public PhoneService(PhoneRepository phoneRepository, OrderEventMapper orderEventMapper) {
        this.phoneRepository = phoneRepository;
        this.orderEventMapper = orderEventMapper;
    }

    public List<PhoneResponseDto> findAll() {
        log.info("Fetching all phones");
        return phoneRepository.findAll()
                .stream()
                .map(orderEventMapper::toPhoneResponseDto)
                .toList();
    }

    public Optional<PhoneResponseDto> findById(Integer phoneId) {
        log.info("Fetching phone with id: {}", phoneId);
        Optional<PhoneDao> byId = phoneRepository.findById(phoneId);
        return phoneRepository.findById(phoneId)
                .map(orderEventMapper::toPhoneResponseDto);
    }

    @Transactional
    public PhoneResponseDto create(PhoneRequestDto phoneRequestDto) {
        log.info("Creating phone: {}", phoneRequestDto);
        PhoneDao phone = orderEventMapper.toPhoneEntity(phoneRequestDto);
        PhoneDao savedPhone = phoneRepository.save(phone);
        log.info("Successfully created phone: {}", savedPhone);
        PhoneResponseDto phoneResponseDto = orderEventMapper.toPhoneResponseDto(savedPhone);
        return phoneResponseDto;
    }

    @Transactional
    public Optional<PhoneResponseDto> update(Integer orderId, PhoneRequestDto phoneRequestDto) {
        log.info("Updating phone with id: {}", orderId);
        return phoneRepository.findById(orderId)
                .map(existingPhone -> {
                    existingPhone.setPhoneId(phoneRequestDto.phoneId());
                    existingPhone.setPhoneName(phoneRequestDto.phoneName());
                    existingPhone.setPhoneModel(phoneRequestDto.phoneModel());
                    existingPhone.setPhonePrice(phoneRequestDto.phonePrice());
                    existingPhone.setPhoneManufacturer(phoneRequestDto.phoneManufacturer());
                    PhoneDao updatedPhone = phoneRepository.save(existingPhone);
                    log.info("Successfully updated phone: {}", updatedPhone);
                    return orderEventMapper.toPhoneResponseDto(updatedPhone);
                });
    }

    @Transactional
    public boolean delete(Integer orderId) {
        log.info("Deleting order with id: {}", orderId);
        return phoneRepository.findById(orderId)
                .map(phone -> {
                    OrderEventDao orderEvent = phone.getOrderEvent();

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


