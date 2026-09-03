package com.flowsync.service;

import com.flowsync.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {

    void log(Long userId,
             String userEmail,
             String action,
             String entityType,
             Long entityId,
             String oldValue,
             String newValue,
             String details);

    List<AuditLogResponse> getAllLogs();

    List<AuditLogResponse> getLogsByEntity(String entityType, Long entityId);
}
