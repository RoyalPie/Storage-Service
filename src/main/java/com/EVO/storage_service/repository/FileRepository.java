package com.EVO.storage_service.repository;

import com.EVO.storage_service.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByExtensionType(String extensionType);

    @Query("SELECT f FROM File f WHERE f.accessType = 'public' AND f.id = :id")
    Optional<File> findPublicFile(@Param("id")Long id);

    @Query("SELECT f FROM File f WHERE f.accessType = 'private' AND f.id = :id")
    Optional<File> findPrivateFile(@Param("id")Long id);

    @Query("SELECT f FROM File f WHERE f.MIMEType LIKE 'image/%' AND f.storageFileName = :name AND f.accessType = :accessType")
    Optional<File> findImage(@Param("name") String name, @Param("accessType") String accessType);

    @Query("SELECT f FROM File f WHERE " +
            "(f.accessType = :accessType) AND " +
            "(:extensionType IS NULL OR f.extensionType = :extensionType) AND " +
            "(:ownerId IS NULL OR f.ownerId = :ownerId) AND " +
            "(:dateFilterMode IS NULL OR "+
            "((:dateFilterMode = 'created' AND f.createdDate >= :startDate AND f.createdDate <= :endDate) OR "+
            "(:dateFilterMode = 'updated' AND f.lastModifiedDate >= :startDate AND f.lastModifiedDate <= :endDate)))"
    )
    Page<File> searchPublicFiles(@Param("accessType") String accessType,
                                 @Param("extensionType") String extensionType,
                                 @Param("ownerId") String ownerId,
                                 @Param("dateFilterMode") String dateFilterMode,
                                 @Param("startDate") Instant startDate,
                                 @Param("endDate") Instant endDate,
                                 Pageable pageable);
}
