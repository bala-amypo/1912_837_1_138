package com.example.demo.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private DigitalKey digitalKey;

    @ManyToOne(optional = false)
    private Guest guest;

    private Timestamp accessTime;

    private String result;
    private String reason;

    public AccessLog() {}

    public AccessLog(DigitalKey digitalKey, Guest guest,
                     Timestamp accessTime, String result,
                     String reason) {
        this.digitalKey = digitalKey;
        this.guest = guest;
        this.accessTime = accessTime;
        this.result = result;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public DigitalKey getDigitalKey() { return digitalKey; }
    public Guest getGuest() { return guest; }
    public Timestamp getAccessTime() { return accessTime; }
    public String getResult() { return result; }
    public String getReason() { return reason; }

    public void setDigitalKey(DigitalKey digitalKey) {
        this.digitalKey = digitalKey;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public void setAccessTime(Instant instant) {
        this.accessTime = instant == null ? null : Timestamp.from(instant);
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
