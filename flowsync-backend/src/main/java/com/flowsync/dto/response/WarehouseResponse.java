package com.flowsync.dto.response;

import com.flowsync.entity.Warehouse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {
    private Long id;
    private String code;
    private String name;
    private String location;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WarehouseResponse from(Warehouse wh) {
        return WarehouseResponse.builder()
                .id(wh.getId())
                .code(wh.getCode())
                .name(wh.getName())
                .location(wh.getLocation())
                .active(wh.getActive())
                .createdAt(wh.getCreatedAt())
                .updatedAt(wh.getUpdatedAt())
                .build();
    }
}
