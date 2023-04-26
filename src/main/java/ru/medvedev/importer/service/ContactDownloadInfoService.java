package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.ContactDownloadInfoEntity;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.DownloadFilterEntity;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.id.InnExcludeId;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.DownloadFilter;
import ru.medvedev.importer.repository.ContactDownloadInfoRepository;
import ru.medvedev.importer.repository.InnExcludeRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static ru.medvedev.importer.enums.Bank.VTB;

@Service
@RequiredArgsConstructor
public class ContactDownloadInfoService {

    private static final int MAX_BATCH_SIZE = 1000;

    private final ContactDownloadInfoRepository repository;
    private final ContactNewService contactNewService;
    private final DownloadFilterService downloadFilterService;
    private final FirstLoadLeadService firstLoadLeadService;
    private final InnExcludeRepository innExcludeRepository;

    public void save(List<ContactDownloadInfoEntity> entityList) {
        repository.saveAll(entityList);
    }

    public void createDownloadByBanks(List<String> innSet, List<FileInfoBankEntity> fileInfoBankEntityList) {
        List<ContactDownloadInfoEntity> downloadInfoList = new ArrayList<>();

        for (int i = 0; i < innSet.size(); i += MAX_BATCH_SIZE) {
            List<ContactNewEntity> contacts = contactNewService.getContactIdByInn(innSet.subList(i,
                    Math.min((i + MAX_BATCH_SIZE), innSet.size())));

            fileInfoBankEntityList.forEach(fileInfoBank -> {
                List<DownloadFilterEntity> innFilters = downloadFilterService.getByCodeAndBank(DownloadFilter.INN, fileInfoBank.getBank());
                List<DownloadFilterEntity> phoneFilters = downloadFilterService.getByCodeAndBank(DownloadFilter.PHONE, fileInfoBank.getBank());

                contacts.stream()
                        .filter(contact -> innFilters.stream()
                                .noneMatch(filter -> contact.getInn().startsWith(filter.getValue())))
                        .filter(contact -> phoneFilters.stream()
                                .noneMatch(filter -> contact.getPhone().startsWith(filter.getValue())))
                        .filter(contact -> fileInfoBank.getBank() != VTB ||
                                !fileInfoBank.getFileInfo().getNotLoadFromVtbDedublicate() ||
                                !firstLoadLeadService.existsByInn(contact.getInn()))
                        .filter(contact -> { //inn-exclude
                            InnExcludeId id = new InnExcludeId();
                            id.setInn(contact.getInn());
                            id.setBank(fileInfoBank.getBank());
                            return !innExcludeRepository.existsById(id);
                        })
                        .forEach(contact -> {
                            ContactDownloadInfoEntity downloadInfoEntity = new ContactDownloadInfoEntity();
                            downloadInfoEntity.setContactId(contact.getId());
                            downloadInfoEntity.setFileInfoBankId(fileInfoBank.getId());
                            if (fileInfoBank.getBank() == Bank.RAIFFEISEN) {
                                downloadInfoEntity.setCheckStatus(ContactStatus.DOWNLOADED);
                            }
                            downloadInfoList.add(downloadInfoEntity);
                        });
            });
        }
        repository.saveAll(downloadInfoList);
    }

    public void deleteAllByFileInfoBankId(Collection<Long> fibIds) {
        repository.deleteAllByFileInfoBankIdIn(fibIds);
    }
}
