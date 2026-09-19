package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByToken(String token);
    List<Session> findByUserId(Integer userId);
}