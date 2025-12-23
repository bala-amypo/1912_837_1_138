package com.example.demo.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

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

    public AccessLog() {
    }

    public AccessLog(DigitalKey digitalKey, Guest guest,
                     Timestamp accessTime, String result,
                     String reason) {

        if (accessTime != null &&
                accessTime.after(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Access time cannot be in the future");
        }

        this.digitalKey = digitalKey;
        this.guest = guest;
        this.accessTime = accessTime;
        this.result = result;
        this.reason = reason;
    }

    // getters
    public Long getId() { return id; }
    public DigitalKey getDigitalKey() { return digitalKey; }
    public Guest getGuest() { return guest; }
    public Timestamp getAccessTime() { return accessTime; }
    public String getResult() { return result; }
    public String getReason() { return reason; }
}
