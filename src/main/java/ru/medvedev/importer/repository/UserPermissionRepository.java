package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.UserPermissionEntity;
import ru.medvedev.importer.entity.UserPermissionId;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, UserPermissionId> {

    @Modifying
    @Transactional
    @Query("delete from UserPermissionEntity up where up.id.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("select up from UserPermissionEntity up where up.id.userId = :userId")
    List<UserPermissionEntity> findByUserId(@Param("userId") Long userId);
}
