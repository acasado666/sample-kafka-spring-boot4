package com.kodebytes.acasado.repository;

import com.kodebytes.acasado.entity.PhoneDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<PhoneDao, Integer> {
}

