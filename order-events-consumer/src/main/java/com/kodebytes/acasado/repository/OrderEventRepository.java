package com.kodebytes.acasado.repository;

import com.kodebytes.acasado.entity.OrderEventDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventRepository extends JpaRepository<OrderEventDao, Integer> {
}

