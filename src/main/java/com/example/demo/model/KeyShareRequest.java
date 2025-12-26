package com.example.demo.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "key_share_requests")
public class KeyShareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private DigitalKey digitalKey;

    @ManyToOne(optional = false)
    private Guest sharedBy;

    @ManyToOne(optional = false)
    private Guest sharedWith;

    private Timestamp shareStart;
    private Timestamp shareEnd;

    private String status = "PENDING";

    private Timestamp createdAt;

    public KeyShareRequest() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        if (this.status == null) this.status = "PENDING";
    }

    public Long getId() { return id; }
    public DigitalKey getDigitalKey() { return digitalKey; }
    public Guest getSharedBy() { return sharedBy; }
    public Guest getSharedWith() { return sharedWith; }
    public Timestamp getShareStart() { return shareStart; }
    public Timestamp getShareEnd() { return shareEnd; }
    public String getStatus() { return status; }

    public void setDigitalKey(DigitalKey digitalKey) {
        this.digitalKey = digitalKey;
    }

    public void setSharedBy(Guest sharedBy) {
        this.sharedBy = sharedBy;
    }

    public void setSharedWith(Guest sharedWith) {
        this.sharedWith = sharedWith;
    }

    public void setShareStart(Instant instant) {
        this.shareStart = instant == null ? null : Timestamp.from(instant);
    }

    public void setShareEnd(Instant instant) {
        this.shareEnd = instant == null ? null : Timestamp.from(instant);
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
