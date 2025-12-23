package com.example.demo.controller;

import com.example.demo.model.RoomBooking;
import com.example.demo.service.RoomBookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class RoomBookingController {

    private final RoomBookingService bookingService;

    public RoomBookingController(RoomBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public RoomBooking create(@RequestBody RoomBooking booking) {
        return bookingService.createBooking(booking);
    }

    @GetMapping("/{id}")
    public RoomBooking getById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @GetMapping("/guest/{guestId}")
    public List<RoomBooking> getForGuest(@PathVariable Long guestId) {
        return bookingService.getBookingsForGuest(guestId);
    }

    @PutMapping("/{id}")
    public RoomBooking update(@PathVariable Long id,
                              @RequestBody RoomBooking booking) {
        return bookingService.updateBooking(id, booking);
    }

    @PutMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        bookingService.deactivateBooking(id);
    }
}
