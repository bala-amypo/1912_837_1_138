package com.example.demo.service.impl;

import com.example.demo.model.AccessLog;
import com.example.demo.model.DigitalKey;
import com.example.demo.model.Guest;
import com.example.demo.model.KeyShareRequest;
import com.example.demo.repository.AccessLogRepository;
import com.example.demo.repository.DigitalKeyRepository;
import com.example.demo.repository.GuestRepository;
import com.example.demo.repository.KeyShareRequestRepository;
import com.example.demo.service.AccessLogService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogRepository accessLogRepository;
    private final DigitalKeyRepository digitalKeyRepository;
    private final GuestRepository guestRepository;
    private final KeyShareRequestRepository keyShareRequestRepository;

    public AccessLogServiceImpl(
            AccessLogRepository accessLogRepository,
            DigitalKeyRepository digitalKeyRepository,
            GuestRepository guestRepository,
            KeyShareRequestRepository keyShareRequestRepository
    ) {
        this.accessLogRepository = accessLogRepository;
        this.digitalKeyRepository = digitalKeyRepository;
        this.guestRepository = guestRepository;
        this.keyShareRequestRepository = keyShareRequestRepository;
    }

    @Override
    public AccessLog createLog(AccessLog log) {

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // STRICT future validation (only if clearly future)
        if (log.getAccessTime() != null && log.getAccessTime().after(new Timestamp(now.getTime() + 1000))) {
            throw new IllegalArgumentException("Access time cannot be in the future");
        }

        DigitalKey key = digitalKeyRepository.findById(log.getDigitalKey().getId())
                .orElseThrow(() -> new IllegalArgumentException("Key not found"));

        Guest guest = guestRepository.findById(log.getGuest().getId())
                .orElseThrow(() -> new IllegalArgumentException("Guest not found"));

        boolean allowed = false;

        // Owner access
        if (key.getActive() && key.getBooking().getGuest().getId().equals(guest.getId())) {
            allowed = true;
        }

        // Shared access
        if (!allowed && key.getActive()) {
            List<KeyShareRequest> shares =
                    keyShareRequestRepository.findBySharedWithId(guest.getId());

            for (KeyShareRequest req : shares) {
                if (req.getDigitalKey().getId().equals(key.getId())
                        && "APPROVED".equals(req.getStatus())
                        && !log.getAccessTime().before(req.getShareStart())
                        && !log.getAccessTime().after(req.getShareEnd())) {
                    allowed = true;
                    break;
                }
            }
        }

        AccessLog result = new AccessLog(
                key,
                guest,
                log.getAccessTime(),
                allowed ? "SUCCESS" : "DENIED",
                allowed ? "Access granted" : "Access denied"
        );

        return accessLogRepository.save(result);
    }

    @Override
    public List<AccessLog> getLogsForKey(Long keyId) {
        return accessLogRepository.findByDigitalKeyId(keyId);
    }

    @Override
    public List<AccessLog> getLogsForGuest(Long guestId) {
        return accessLogRepository.findByGuestId(guestId);
    }
}
