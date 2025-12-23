package com.example.demo.controller;

import com.example.demo.model.DigitalKey;
import com.example.demo.service.DigitalKeyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/digital-keys")
public class DigitalKeyController {

    private final DigitalKeyService digitalKeyService;

    public DigitalKeyController(DigitalKeyService digitalKeyService) {
        this.digitalKeyService = digitalKeyService;
    }

    @PostMapping("/generate/{bookingId}")
    public DigitalKey generate(@PathVariable Long bookingId) {
        return digitalKeyService.generateKey(bookingId);
    }

    @GetMapping("/{id}")
    public DigitalKey getById(@PathVariable Long id) {
        return digitalKeyService.getKeyById(id);
    }

    @GetMapping("/booking/{bookingId}")
    public DigitalKey getActiveForBooking(@PathVariable Long bookingId) {
        return digitalKeyService.getActiveKeyForBooking(bookingId);
    }

    @GetMapping("/guest/{guestId}")
    public List<DigitalKey> getForGuest(@PathVariable Long guestId) {
        return digitalKeyService.getKeysForGuest(guestId);
    }
}
