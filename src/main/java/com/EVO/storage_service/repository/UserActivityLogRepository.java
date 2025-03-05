package com.EVO.storage_service.repository;


import com.EVO.storage_service.entity.User;
import com.EVO.storage_service.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    List<UserActivityLog> findByUser(User user);
}
