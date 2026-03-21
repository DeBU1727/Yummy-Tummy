package com.yummytummy.backend.repository;

import com.yummytummy.backend.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndCode(String email, String code);
    Optional<Otp> findTopByEmailOrderByExpirationTimeDesc(String email);
}
