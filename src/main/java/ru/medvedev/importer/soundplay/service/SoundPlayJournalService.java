package ru.medvedev.importer.soundplay.service;

import lombok.RequiredArgsConstructor;
import ru.medvedev.importer.soundplay.dto.SoundPlayJournalDto;
import ru.medvedev.importer.soundplay.entity.SoundPlayJournalEntity;
import ru.medvedev.importer.soundplay.repository.SoundPlayJournalRepository;

//@Service
@RequiredArgsConstructor
public class SoundPlayJournalService {

    private final SoundPlayJournalRepository repository;

    public void startSound(SoundPlayJournalDto dto) {
        SoundPlayJournalEntity entity = new SoundPlayJournalEntity();
        entity.setName(dto.getName());
        entity.setDuration(dto.getDuration());
        entity.setRequestId(dto.getId());
        entity.setUtcMillisStart(dto.getUtcmillis());
        entity.setUserId(dto.getUserid());
        if (!repository.findByRequestId(dto.getId()).isPresent()) {
            repository.save(entity);
        }
    }

    public void stopSound(SoundPlayJournalDto dto) {
        repository.findByRequestId(dto.getId()).ifPresent(item -> {
            item.setUtcMillisStop(dto.getUtcmillis());
            repository.save(item);
        });
    }
}
