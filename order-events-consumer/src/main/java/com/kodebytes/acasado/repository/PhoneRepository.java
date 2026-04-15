package com.kodebytes.acasado.repository;

import com.kodebytes.acasado.domain.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
}

