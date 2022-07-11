package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.PermissionDto;
import ru.medvedev.importer.dto.UserDto;
import ru.medvedev.importer.dto.UserInput;
import ru.medvedev.importer.entity.UserEntity;
import ru.medvedev.importer.enums.Permission;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserPermissionService userPermissionService;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDto> getPage(Pageable pageable) {
        Page<UserEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(entity -> UserDto.of(entity, userPermissionService.getAll(entity.getId())))
                .collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public UserDto getById(Long id) {
        UserEntity entity = repository.findById(id).orElseThrow(
                EntityNotFoundException::new);
        return UserDto.of(entity, userPermissionService.getAll(entity.getId()));
    }

    public List<PermissionDto> getPermissionList() {
        return Arrays.stream(Permission.values())
                .sorted(Comparator.comparing(Enum::name))
                .map(permission -> {
                    PermissionDto dto = new PermissionDto();
                    dto.setCode(permission.name());
                    dto.setDescription(permission.getDescription());
                    return dto;
                }).collect(Collectors.toList());
    }

    public void create(UserInput dto) {

        if (repository.existsByLogin(dto.getLogin())) {
            throw new BadRequestException("Пользователь с таким логином уже существует");
        }

        UserEntity entity = new UserEntity();
        entity.setFio(dto.getFio());
        entity.setLogin(dto.getLogin());
        entity.setActive(true);
        if (isNotBlank(dto.getPassword())) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        entity.setPassword(dto.getPassword());
        entity = repository.save(entity);
        userPermissionService.setPermissionToUser(entity.getId(), dto.getPermissions());
    }

    public void update(Long id, UserInput dto) {
        UserEntity entity = repository.findById(id).orElseThrow(
                EntityNotFoundException::new);
        if (isNotBlank(dto.getPassword())) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        entity.setFio(dto.getFio());
        entity = repository.save(entity);
        userPermissionService.setPermissionToUser(entity.getId(), dto.getPermissions());
    }

    public void blockUser(Long id) {
        UserEntity user = repository.findById(id).orElseThrow(
                EntityNotFoundException::new);
        user.setActive(false);
        repository.save(user);
    }

    public void unblockUser(Long id) {
        UserEntity user = repository.findById(id).orElseThrow(
                EntityNotFoundException::new);
        user.setActive(true);
        repository.save(user);
    }
}
