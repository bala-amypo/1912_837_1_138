package com.example.demo.service;

import com.example.demo.model.AccessLog;

import java.util.List;

public interface AccessLogService {

    AccessLog createLog(AccessLog log);

    List<AccessLog> getLogsForKey(Long keyId);

    List<AccessLog> getLogsForGuest(Long guestId);
}
