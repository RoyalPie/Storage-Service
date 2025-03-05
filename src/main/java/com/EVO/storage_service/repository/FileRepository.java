package com.EVO.storage_service.repository;

import com.EVO.storage_service.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByExtensionType(String extensionType);

    Optional<File> findById(Long id);

    @Query("SELECT f FROM File f WHERE f.MIMEType LIKE 'image/%' AND f.id = :id")
    Optional<File> findImage(@Param("id") Long id);

    @Query("SELECT f FROM File f WHERE " +
            "(:extensionType IS NULL OR f.extensionType = :extensionType) AND " +
            "(:ownerId IS NULL OR f.ownerId = :ownerId) AND " +
            "(:filterDate IS NULL OR " +
            "(:dateFilterMode = 'created' AND f.createdDate = :filterDate) OR " +
            "(:dateFilterMode = 'updated' AND f.lastModifiedDate = :filterDate))")
    //whitelist extension file, size file, MIME TYPE
    Page<File> searchPublicFiles(@Param("extensionType") String extensionType,
                                 @Param("ownerId") String ownerId,
                                 @Param("dateFilterMode") String dateFilterMode,
                                 @Param("filterDate") Instant filterDate,
                                 Pageable pageable);

}
