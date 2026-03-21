package com.yummytummy.backend.repository;

import com.yummytummy.backend.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer> {
    Optional<Offer> findByOfferCode(String offerCode);
}
