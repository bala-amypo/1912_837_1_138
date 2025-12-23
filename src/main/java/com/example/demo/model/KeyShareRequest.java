package com.example.demo.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

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

    public KeyShareRequest() {
    }

    public KeyShareRequest(DigitalKey digitalKey, Guest sharedBy,
                           Guest sharedWith, Timestamp shareStart,
                           Timestamp shareEnd, String status) {

        if (shareEnd != null && shareStart != null &&
                shareEnd.before(shareStart)) {
            throw new IllegalArgumentException("Share end must be after start");
        }

        if (sharedBy != null && sharedWith != null &&
                sharedBy.getId().equals(sharedWith.getId())) {
            throw new IllegalArgumentException("sharedBy and sharedWith cannot be same");
        }

        this.digitalKey = digitalKey;
        this.sharedBy = sharedBy;
        this.sharedWith = sharedWith;
        this.shareStart = shareStart;
        this.shareEnd = shareEnd;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        if (this.status == null) this.status = "PENDING";
    }

    // getters
    public Long getId() { return id; }
    public DigitalKey getDigitalKey() { return digitalKey; }
    public Guest getSharedBy() { return sharedBy; }
    public Guest getSharedWith() { return sharedWith; }
    public Timestamp getShareStart() { return shareStart; }
    public Timestamp getShareEnd() { return shareEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
