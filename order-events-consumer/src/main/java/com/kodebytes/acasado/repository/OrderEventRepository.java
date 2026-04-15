package com.kodebytes.acasado.repository;

import com.kodebytes.acasado.domain.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
}

