package com.example.demo.service;

import com.example.demo.model.RoomBooking;

import java.util.List;

public interface RoomBookingService {

    RoomBooking createBooking(RoomBooking booking);

    RoomBooking updateBooking(Long id, RoomBooking booking);

    RoomBooking getBookingById(Long id);

    List<RoomBooking> getBookingsForGuest(Long guestId);

    void deactivateBooking(Long id);
}
