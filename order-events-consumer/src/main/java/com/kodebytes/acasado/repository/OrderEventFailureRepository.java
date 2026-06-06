package com.kodebytes.acasado.repository;

import com.kodebytes.acasado.entity.OrderEventFailure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventFailureRepository extends JpaRepository<OrderEventFailure, Integer> {

    List<OrderEventFailure> findAllByStatus(String status);
}
