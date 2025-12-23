package com.example.demo.service.impl;

import com.example.demo.exception.ResourceNotFoundException;
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

        // Validate future access time
        if (log.getAccessTime().after(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("future");
        }

        // Fetch digital key
        DigitalKey key = digitalKeyRepository.findById(log.getDigitalKey().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Key not found"));

        // Fetch guest
        Guest guest = guestRepository.findById(log.getGuest().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));

        boolean allowed = false;

        // Case 1: Booking owner
        if (key.getBooking().getGuest().getId().equals(guest.getId())) {
            allowed = true;
        }

        // Case 2: Shared access
        if (!allowed) {
            List<KeyShareRequest> shares =
                    keyShareRequestRepository.findBySharedWithId(guest.getId());

            for (KeyShareRequest req : shares) {
                if (req.getDigitalKey().getId().equals(key.getId())
                        && "APPROVED".equals(req.getStatus())
                        && log.getAccessTime().after(req.getShareStart())
                        && log.getAccessTime().before(req.getShareEnd())) {
                    allowed = true;
                    break;
                }
            }
        }

        AccessLog resultLog;

        if (allowed) {
            resultLog = new AccessLog(
                    key,
                    guest,
                    log.getAccessTime(),
                    "SUCCESS",
                    "Access granted"
            );
        } else {
            resultLog = new AccessLog(
                    key,
                    guest,
                    log.getAccessTime(),
                    "DENIED",
                    "Access denied"
            );
        }

        return accessLogRepository.save(resultLog);
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
