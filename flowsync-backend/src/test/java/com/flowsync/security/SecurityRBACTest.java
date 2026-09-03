package com.flowsync.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRBACTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request to protected admin endpoint returns 403 or 401")
    void unauthenticated_adminEndpoint_fails() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("CUSTOMER role accessing /api/admin/dashboard returns 403 Forbidden")
    void customer_accessingAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("ADMIN role accessing /api/admin/dashboard returns 200 OK")
    void admin_accessingAdmin_allowed() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("CUSTOMER role accessing /api/transfers returns 403 Forbidden")
    void customer_accessingTransfers_forbidden() throws Exception {
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "warehouse@example.com", roles = {"WAREHOUSE_MANAGER"})
    @DisplayName("WAREHOUSE_MANAGER role accessing /api/transfers returns 200 OK")
    void warehouseManager_accessingTransfers_allowed() throws Exception {
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("CUSTOMER role accessing /api/audit-logs returns 403 Forbidden")
    void customer_accessingAuditLogs_forbidden() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("ADMIN role accessing /api/audit-logs returns 200 OK")
    void admin_accessingAuditLogs_allowed() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk());
    }
}
