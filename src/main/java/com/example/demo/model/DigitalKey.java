package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "digital_keys", uniqueConstraints = {
        @UniqueConstraint(columnNames = "keyValue")
})
public class DigitalKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private RoomBooking booking;

    @Column(nullable = false, unique = true)
    private String keyValue;

    // 🔥 CHANGE HERE
    private Instant issuedAt;
    private Instant expiresAt;

    private Boolean active = true;

    public DigitalKey() {
    }

    public DigitalKey(RoomBooking booking, String keyValue,
                      Instant issuedAt, Instant expiresAt,
                      Boolean active) {

        if (expiresAt != null && issuedAt != null &&
                expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("Key expiration must be after issue time");
        }

        this.booking = booking;
        this.keyValue = keyValue;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        if (this.active == null) this.active = true;
    }

    // getters and setters
    public Long getId() { return id; }

    // Optional but useful for tests
    public void setId(Long id) { this.id = id; }

    public RoomBooking getBooking() { return booking; }
    public void setBooking(RoomBooking booking) { this.booking = booking; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
