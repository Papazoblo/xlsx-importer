package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.ContactDownloadInfoEntity;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.repository.ContactDownloadInfoRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactDownloadInfoService {

    private static final int MAX_BATCH_SIZE = 1000;

    private final ContactDownloadInfoRepository repository;
    private final ContactNewService contactNewService;

    public void save(List<ContactDownloadInfoEntity> entityList) {
        repository.saveAll(entityList);
    }

    public void createDownloadByBanks(List<String> innSet, List<FileInfoBankEntity> fileInfoBankEntityList) {
        List<ContactDownloadInfoEntity> downloadInfoList = new ArrayList<>();

        for (int i = 0; i < innSet.size(); i += MAX_BATCH_SIZE) {
            List<Long> contactIds = contactNewService.getContactIdByInn(innSet.subList(i,
                    Math.min((i + MAX_BATCH_SIZE), innSet.size())));
            contactIds.forEach(contactId -> fileInfoBankEntityList.forEach(fileInfoBank -> {
                ContactDownloadInfoEntity downloadInfoEntity = new ContactDownloadInfoEntity();
                downloadInfoEntity.setContactId(contactId);
                downloadInfoEntity.setFileInfoBankId(fileInfoBank.getId());
                downloadInfoList.add(downloadInfoEntity);
            }));
        }
        repository.saveAll(downloadInfoList);
    }
}
