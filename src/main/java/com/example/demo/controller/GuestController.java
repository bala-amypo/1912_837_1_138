package com.example.demo.controller;

import com.example.demo.model.Guest;
import com.example.demo.service.GuestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public Guest create(@RequestBody Guest guest) {
        return guestService.createGuest(guest);
    }

    @GetMapping("/{id}")
    public Guest getById(@PathVariable Long id) {
        return guestService.getGuestById(id);
    }

    @GetMapping
    public List<Guest> getAll() {
        return guestService.getAllGuests();
    }

    @PutMapping("/{id}")
    public Guest update(@PathVariable Long id, @RequestBody Guest guest) {
        return guestService.updateGuest(id, guest);
    }

    @PutMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        guestService.deactivateGuest(id);
    }
}
