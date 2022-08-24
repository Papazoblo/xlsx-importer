package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.ContactDto;
import ru.medvedev.importer.dto.ContactStatistic;
import ru.medvedev.importer.dto.WebhookStatusDto;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.repository.ContactRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Integer BATCH_SIZE = 100;

    private final ContactRepository repository;
    private final ContactFileInfoService contactFileInfoService;
    private final WebhookStatusService webhookStatusService;

    public Page<ContactDto> getPage(Pageable pageable) {
        Page<ContactEntity> page = repository.findAll(pageable);
        Map<Long, Boolean> contactOriginalMap = contactFileInfoService.getOriginalsMap(page.getContent().stream()
                .map(ContactEntity::getId).collect(Collectors.toSet()));
        return new PageImpl<>(page.getContent().stream()
                .map(item -> ContactDto.of(item, contactOriginalMap.get(item.getId()))).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public void changeWebhookStatus(String inn, String resultName) {
        WebhookStatusDto webhookStatus = webhookStatusService.getByName(resultName);
        repository.changeWebhookStatus(webhookStatus.getId(), inn);
    }

    public List<ContactEntity> filteredContacts(List<ContactEntity> contacts, Long fileId) {
        List<String> contactInn = contacts.stream()
                .filter(entity -> isNotBlank(entity.getInn()))
                .map(ContactEntity::getInn)
                .collect(Collectors.toList());
        Map<String, List<ContactEntity>> contactMap = repository.findAllByInnIn(contactInn).stream()
                .collect(groupingBy(ContactEntity::getInn));

        //сохранение оригинальных контактов
        List<ContactEntity> originalContact = new ArrayList<>();
        List<ContactEntity> duplicatedContact = new ArrayList<>();
        contacts.stream()
                .filter(entity -> isNotBlank(entity.getInn()))
                .forEach(contact -> {
                    contact.setStatus(ContactStatus.ADDED);
                    if (contactMap.get(contact.getInn()) == null) {
                        originalContact.add(contact);
                    } else {
                        duplicatedContact.add(contact);
                    }
                });

        List<ContactEntity> savedOriginal = createNew(originalContact);
        List<ContactEntity> savedDuplicate = createNew(duplicatedContact);

        contactFileInfoService.create(savedOriginal, fileId, true);
        contactFileInfoService.create(savedDuplicate, fileId, false);
        return originalContact;
    }

    private List<ContactEntity> createNew(List<ContactEntity> contacts) {
        WebhookStatusEntity webhookStatus = webhookStatusService.getById(-1L);
        contacts.forEach(item -> item.setWebhookStatus(webhookStatus));
        return repository.saveAll(contacts);
    }

    public void changeContactStatus(List<LeadInfoResponse> leads, Long fileId, ContactStatus status) {
        for (int i = 0; i < leads.size(); i += BATCH_SIZE) {
            List<String> innList = leads.subList(i, Math.min(i + BATCH_SIZE, leads.size())).stream()
                    .map(LeadInfoResponse::getInn).collect(Collectors.toList());
            List<Long> contactIds = repository.findContactIdByInn(fileId, innList);
            repository.changeContactStatusById(status, contactIds);
        }
    }

    public Map<ContactStatus, Long> getContactStatisticByFileId(Long fileId) {
        return repository.getContactStatisticByFileId(fileId).stream()
                .collect(toMap(ContactStatistic::getStatus, ContactStatistic::getCount));
    }
}
