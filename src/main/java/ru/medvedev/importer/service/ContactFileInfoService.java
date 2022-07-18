package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.ContactFileInfoEntity;
import ru.medvedev.importer.entity.ContactFileInfoId;
import ru.medvedev.importer.repository.ContactFileInfoRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


@Service
@RequiredArgsConstructor
public class ContactFileInfoService {

    private final ContactFileInfoRepository repository;

    public void create(List<ContactEntity> contacts, Long fileId, boolean isOriginal) {
        repository.saveAll(contacts.stream().map(contact -> {
            ContactFileInfoEntity entity = new ContactFileInfoEntity();
            entity.setOriginal(isOriginal);
            entity.setId(ContactFileInfoId.of(fileId, contact.getId()));
            return entity;
        }).collect(Collectors.toList()));
    }

    public Map<Long, Boolean> getOriginalsMap(Set<Long> contactIds) {
        return repository.findByContactId(contactIds).stream()
                .collect(toMap(item -> item.getId().getContactId(),
                        ContactFileInfoEntity::isOriginal));
    }
}
