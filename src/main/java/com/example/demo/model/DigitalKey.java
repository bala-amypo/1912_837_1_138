package com.example.demo.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

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

    private Timestamp issuedAt;
    private Timestamp expiresAt;

    private Boolean active = true;

    public DigitalKey() {
    }

    public DigitalKey(RoomBooking booking, String keyValue,
                      Timestamp issuedAt, Timestamp expiresAt,
                      Boolean active) {
        if (expiresAt != null && issuedAt != null &&
                expiresAt.before(issuedAt)) {
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
    public RoomBooking getBooking() { return booking; }
    public void setBooking(RoomBooking booking) { this.booking = booking; }
    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }
    public Timestamp getIssuedAt() { return issuedAt; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
