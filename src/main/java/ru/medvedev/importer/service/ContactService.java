package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.repository.ContactRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Integer BATCH_SIZE = 100;

    private final ContactRepository repository;
    private final ContactFileInfoService contactFileInfoService;

    public List<ContactEntity> filteredContacts(List<ContactEntity> contacts, Long fileId) {
        List<String> contactInn = contacts.stream()
                .map(ContactEntity::getInn)
                .collect(Collectors.toList());
        Map<String, List<ContactEntity>> contactMap = repository.findAllByInnIn(contactInn).stream()
                .collect(groupingBy(ContactEntity::getInn));

        //сохранение оригинальных контактов
        List<ContactEntity> originalContact = new ArrayList<>();
        List<ContactEntity> duplicatedContact = new ArrayList<>();
        contacts.forEach(contact -> {
            contact.setStatus(ContactStatus.ADDED);
            if (contactMap.get(contact.getInn()) == null) {
                originalContact.add(contact);
            } else {
                duplicatedContact.add(contact);
            }
        });
        contactFileInfoService.create(originalContact, fileId, true);
        contactFileInfoService.create(duplicatedContact, fileId, false);
        return originalContact;
    }

    public void rejectingContact(List<LeadInfoResponse> leads, Long fileId) {

        for (int i = 0; i < leads.size(); i += BATCH_SIZE) {
            List<String> innList = leads.subList(i, Math.min(BATCH_SIZE, leads.size() - 1)).stream()
                    .map(LeadInfoResponse::getInn).collect(Collectors.toList());
            List<Long> contactIds = repository.findContactIdByInn(fileId, innList);
            repository.changeContactStatusById(ContactStatus.REJECTED, contactIds);
        }
        //todo забракованные контакты
    }
}
