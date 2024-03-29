package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.repository.ContactRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Integer BATCH_SIZE = 100;

    private final ContactRepository repository;
    private final ContactFileInfoService contactFileInfoService;
    private final WebhookStatusService webhookStatusService;

    /*public Page<ContactDto> getPage(ContactFilter filter, Pageable pageable) {
        Page<ContactEntity> page = repository.findAll(ContactSpecification.of(filter), pageable);
        Map<Long, Boolean> contactOriginalMap = contactFileInfoService.getOriginalsMap(page.getContent().stream()
                .map(ContactEntity::getId).collect(Collectors.toSet()));
        return new PageImpl<>(page.getContent().stream()
                .map(item -> ContactDto.of(item, contactOriginalMap.get(item.getId()))).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public List<ContactEntity> findAll(ContactFilter filter) {
        return repository.findAll(ContactSpecification.of(filter));
    }*/

    public Optional<ContactEntity> findLastByInn(String inn) {
        return repository.findFirstByInnOrderByIdDesc(inn);
    }

    public void changeWebhookStatus(String inn, WebhookSuccessStatusEntity webhookStatus) {
        repository.changeWebhookStatus(webhookStatus.getId(), inn, webhookStatus.getBank().name());
    }

    /*public List<ContactEntity> filteredContacts(List<ContactEntity> contacts, FileInfoBankEntity fileBank) {
        List<String> contactInn = contacts.stream()
                .map(ContactEntity::getInn)
                .collect(Collectors.toList());
        Map<String, List<ContactEntity>> contactMap = repository.findAllByInnInAndBank(contactInn, fileBank.getBank()).stream()
                .collect(groupingBy(ContactEntity::getInn));

        List<ContactEntity> originalContact = new ArrayList<>();
        List<ContactEntity> duplicatedContact = new ArrayList<>();

        //сохранение оригинальных контактов
        contacts.stream()
                .filter(contact -> checkLength(contact.getOrgName(), 1000)
                        && checkLength(contact.getName(), 100)
                        && checkLength(contact.getSurname(), 100)
                        && checkLength(contact.getMiddleName(), 100)
                        && checkLength(contact.getPhone(), 20)
                        && checkLength(contact.getInn(), 20)
                        && checkLength(contact.getOgrn(), 20)
                        && checkLength(contact.getRegion(), 500)
                        && checkLength(contact.getCity(), 1000))
                .forEach(contact -> {
                    ContactEntity newContact = contact.clone();
                    newContact.setStatus(ContactStatus.IN_CHECK);
                    newContact.setBank(fileBank.getBank());
                    newContact.setFileInfoBankDownload(fileBank);
                    if (contactMap.get(newContact.getInn()) == null) {
                        newContact.setStatus(ContactStatus.IN_CHECK);
                        originalContact.add(newContact);
                    } else {
                        newContact.setStatus(fileBank.getFileInfo().getSource() == FileSource.TELEGRAM
                                ? ContactStatus.REJECTED : ContactStatus.IN_CHECK);
                        duplicatedContact.add(newContact);
                    }
                });
        List<ContactEntity> savedOriginal = createNew(originalContact);
        List<ContactEntity> savedDuplicate = createNew(duplicatedContact);

        contactFileInfoService.create(savedOriginal, fileBank.getFileInfoId(), true);
        contactFileInfoService.create(savedDuplicate, fileBank.getFileInfoId(), false);

        return originalContact;
    }

    private boolean checkLength(String value, int length) {
        return value == null || value.length() <= length;
    }

    private List<ContactEntity> createNew(List<ContactEntity> contacts) {
        WebhookStatusEntity webhookStatus = webhookStatusService.getById(-1L);
        contacts.forEach(item -> item.setWebhookStatus(webhookStatus));
        return repository.saveAll(contacts);
    }

    public Map<ContactStatus, Long> getContactStatisticByFileId(Long fileId) {
        return repository.getContactStatisticByFileId(fileId).stream()
                .collect(toMap(ContactStatistic::getStatus, ContactStatistic::getCount));
    }*/
}
