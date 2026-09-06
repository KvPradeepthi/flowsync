package com.flowsync.service.impl;

import com.flowsync.dto.response.AuditLogResponse;
import com.flowsync.entity.AuditLog;
import com.flowsync.repository.AuditLogRepository;
import com.flowsync.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for immutable audit logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId,
                    String userEmail,
                    String action,
                    String entityType,
                    Long entityId,
                    String oldValue,
                    String newValue,
                    String details) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail != null ? userEmail : "SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .details(details)
                    .build();

            auditLogRepository.save(entry);
            log.info("[AUDIT] Action: {} on {}:{} by {}", action, entityType, entityId, userEmail);
        } catch (Exception e) {
            log.warn("[AUDIT] Could not record audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
                .stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());
    }
}
