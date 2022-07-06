package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.UserDto;
import ru.medvedev.importer.entity.UserEntity;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public Page<UserDto> getPage(Pageable pageable) {
        Page<UserEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(UserDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public void create(UserDto dto) {

        if (repository.existsByLogin(dto.getLogin())) {
            throw new BadRequestException("");
        }

        UserEntity entity = new UserEntity();
        entity.setFio(dto.getFio());
        entity.setLogin(dto.getLogin());
        entity.setActive(true);
        entity.setPassword(dto.getPassword());
        repository.save(entity);
    }

    public void update(Long id, UserDto dto) {
        UserEntity entity = repository.findById(id).orElseThrow(
                EntityNotFoundException::new);
        entity.setActive(dto.isActive());
        entity.setPassword(dto.getPassword());
        entity.setFio(dto.getFio());
        repository.save(entity);
    }
}
