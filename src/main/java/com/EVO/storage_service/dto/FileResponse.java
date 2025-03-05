package com.EVO.storage_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class FileResponse {
    private String fileName;
    private String ownerId;
    private String accessType;
    private String fileSize;

}
