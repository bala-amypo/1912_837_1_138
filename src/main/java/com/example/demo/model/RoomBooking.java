package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "room_bookings")
public class RoomBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Guest guest;

    private String roomNumber;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "booking_roommates",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "guest_id")
    )
    private Set<Guest> roommates = new HashSet<>();

    public RoomBooking() {
    }

    public RoomBooking(Guest guest, String roomNumber,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       Boolean active) {
        if (checkInDate != null && checkOutDate != null &&
                !checkInDate.isBefore(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        this.guest = guest;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        if (this.active == null) this.active = true;
        if (this.roommates == null) this.roommates = new HashSet<>();
    }

    // getters and setters
    public Long getId() { return id; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Set<Guest> getRoommates() { return roommates; }
}
