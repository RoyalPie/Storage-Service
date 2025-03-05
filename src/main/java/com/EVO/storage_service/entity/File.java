package com.EVO.storage_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class File extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String extensionType;

    @Column(nullable = false, unique = true)
    private String storageFileName;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String accessType;

    @Column
    private String fileSize;

    @Column
    private String MIMEType;


}
