package com.example.demo.service;

import com.example.demo.model.KeyShareRequest;

import java.util.List;

public interface KeyShareRequestService {

    KeyShareRequest createShareRequest(KeyShareRequest request);

    KeyShareRequest updateStatus(Long requestId, String status);

    KeyShareRequest getShareRequestById(Long id);

    List<KeyShareRequest> getRequestsSharedBy(Long guestId);

    List<KeyShareRequest> getRequestsSharedWith(Long guestId);
}
