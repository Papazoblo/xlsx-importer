package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.UserPermissionEntity;
import ru.medvedev.importer.entity.UserPermissionId;
import ru.medvedev.importer.enums.Permission;
import ru.medvedev.importer.repository.UserPermissionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final UserPermissionRepository repository;

    public List<Permission> getAll(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(item -> item.getId().getPermission())
                .collect(Collectors.toList());
    }

    public void setPermissionToUser(Long userId, List<Permission> permissions) {
        repository.deleteByUserId(userId);
        repository.saveAll(permissions.stream().map(permission -> {
            UserPermissionEntity entity = new UserPermissionEntity();
            entity.setId(UserPermissionId.of(userId, permission));
            return entity;
        }).collect(Collectors.toList()));
    }
}
