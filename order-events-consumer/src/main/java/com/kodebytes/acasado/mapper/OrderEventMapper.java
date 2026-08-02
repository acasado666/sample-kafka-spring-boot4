package com.kodebytes.acasado.mapper;

import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.dto.PhoneRequestDto;
import com.kodebytes.acasado.dto.PhoneResponseDto;
import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.entity.OrderEventDao;
import com.kodebytes.acasado.entity.PhoneDao;
import org.springframework.stereotype.Component;

@Component
public class OrderEventMapper {

    public OrderEventDao toEntity(OrderEvent dto) {
        validatePhone(dto);

        OrderEventDao orderEvent = new OrderEventDao();
        orderEvent.setEventType(dto.eventType());

        PhoneDao phone = mapPhone(dto);
        orderEvent.setPhone(phone);

        return orderEvent;
    }

    public void updateEntity(OrderEventDao dao, OrderEventDao existing) {
        validatePhone(dao);

        existing.setEventType(dao.getEventType());

        PhoneDao existingPhone = existing.getPhone();
        if (existingPhone == null) {
            existingPhone = new PhoneDao();
        }

        existingPhone.setPhoneId(dao.getPhone().getPhoneId());
        existingPhone.setPhoneName(dao.getPhone().getPhoneName());
        existingPhone.setPhoneModel(dao.getPhone().getPhoneModel());
        existingPhone.setPhoneManufacturer(dao.getPhone().getPhoneManufacturer());
        existingPhone.setPhonePrice(dao.getPhone().getPhonePrice());
        existing.setPhone(existingPhone);
    }

    public PhoneDao toPhoneEntity(PhoneRequestDto dto) {
        PhoneDao phone = new PhoneDao();
        phone.setPhoneId(dto.phoneId());
        phone.setPhoneName(dto.phoneName());
        phone.setPhoneModel(dto.phoneModel());
        phone.setPhoneManufacturer(dto.phoneManufacturer());
        phone.setPhonePrice(dto.phonePrice());
        return phone;
    }

    public PhoneResponseDto toPhoneResponseDto(PhoneDao phone) {
        Integer orderId = phone.getOrderEvent() != null
                ? phone.getOrderEvent().getOrderId()
                : null;
        return new PhoneResponseDto(
                phone.getPhoneId(),
                phone.getPhoneName(),
                phone.getPhoneModel(),
                phone.getPhoneManufacturer(),
                phone.getPhonePrice(),
                orderId,
                phone.getCreatedAt(),
                phone.getUpdatedAt()
        );
    }

    public OrderEventResponseDto toOrderEventResponseDto(OrderEventDao orderEvent) {
        PhoneResponseDto phone = orderEvent.getPhone() != null
                ? toPhoneResponseDto(orderEvent.getPhone())
                : null;
        return new OrderEventResponseDto(
                orderEvent.getOrderId(),
                orderEvent.getEventType(),
                phone,
                orderEvent.getCreatedAt(),
                orderEvent.getUpdatedAt()
        );
    }

    private PhoneDao mapPhone(OrderEvent dto) {
        PhoneDao phone = new PhoneDao();
        phone.setPhoneId(dto.phone().phoneId());
        phone.setPhoneName(dto.phone().phoneName());
        phone.setPhoneModel(dto.phone().phoneModel());
        phone.setPhoneManufacturer(dto.phone().phoneManufacturer());
        phone.setPhonePrice(dto.phone().phonePrice());
        return phone;
    }

    private void validatePhone(OrderEventDao dto) {
        if (dto.getPhone() == null) {
            throw new IllegalArgumentException("phone is required");
        }
    }

    private void validatePhone(OrderEvent dto) {
        if (dto.phone() == null) {
            throw new IllegalArgumentException("phone is required");
        }
    }
}
