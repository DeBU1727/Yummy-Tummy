package com.yummytummy.backend.controller;

import com.yummytummy.backend.entity.Offer;
import com.yummytummy.backend.repository.OfferRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferRepository offerRepository;
    private final com.yummytummy.backend.service.FileStorageService fileStorageService;

    public OfferController(OfferRepository offerRepository, com.yummytummy.backend.service.FileStorageService fileStorageService) {
        this.offerRepository = offerRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerRepository.findAll());
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateCoupon(@PathVariable String code) {
        try {
            return ResponseEntity.ok(offerRepository.findByOfferCode(code)
                    .map(com.yummytummy.backend.entity.Offer::getDiscountPercentage)
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Offer> addOffer(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("discountPercentage") Integer discountPercentage,
            @RequestParam("offerCode") String offerCode,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        
        Offer offer = new Offer();
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setDiscountPercentage(discountPercentage);
        offer.setOfferCode(offerCode);
        
        if (file != null && !file.isEmpty()) {
            offer.setImageUrl(fileStorageService.storeFile(file));
        }
        
        return ResponseEntity.ok(offerRepository.save(offer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("discountPercentage") Integer discountPercentage,
            @RequestParam("offerCode") String offerCode,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
        
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setDiscountPercentage(discountPercentage);
        offer.setOfferCode(offerCode);
        
        if (file != null && !file.isEmpty()) {
            offer.setImageUrl(fileStorageService.storeFile(file));
        }
        
        return ResponseEntity.ok(offerRepository.save(offer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Integer id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
        offerRepository.delete(offer);
        return ResponseEntity.ok().build();
    }
}
