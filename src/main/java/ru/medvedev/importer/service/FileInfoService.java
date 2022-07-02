package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import ru.medvedev.importer.dto.FileInfoDto;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.FileStatus;
import ru.medvedev.importer.repository.FileInfoRepository;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileInfoService {

    private final FileInfoRepository repository;

    public Page<FileInfoDto> getPage(Pageable pageable) {
        Page<FileInfoEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(FileInfoDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public boolean create(Document document, File file) {

        String hash = hashFile(file.toPath());
        if (!repository.existsByHashAndStatus(hash, FileStatus.SUCCESS)) {
            FileInfoEntity entity = new FileInfoEntity();
            entity.setName(document.getFileName());
            entity.setSize(Long.valueOf(document.getFileSize()));
            entity.setType(document.getMimeType());
            entity.setUniqueId(document.getFileUniqueId());
            entity.setTgFileId(document.getFileId());
            entity.setHash(hash);
            entity.setPath(file.getPath());
            entity.setDeleted(false);
            repository.save(entity);
            log.debug("*** create file with hash {}", hash);
            return true;
        } else {
            log.debug("*** file with hash {} already exist", hash);
            return false;
        }
    }

    public void delete(Long id) {
        FileInfoEntity entity = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        entity.setDeleted(true);
        repository.save(entity);
        new File(entity.getPath()).delete();
    }

    public FileInfoEntity getById(Long id) {
        return repository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public boolean isExistsInProcess() {
        return repository.existsByStatus(FileStatus.IN_PROCESS);
    }

    public Optional<FileInfoEntity> getDownloadedFile() {
        return repository.findFirstByStatusOrderByCreateAt(FileStatus.DOWNLOADED);
    }

    public FileInfoEntity changeStatus(FileInfoEntity entity, FileStatus status) {
        repository.changeStatus(entity.getId(), status);
        entity.setStatus(status);
        return entity;
    }

    @SneakyThrows
    public static String hashFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return md5Hex(is);
        }
    }
}
