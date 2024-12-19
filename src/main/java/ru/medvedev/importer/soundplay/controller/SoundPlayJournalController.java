package ru.medvedev.importer.soundplay.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import ru.medvedev.importer.soundplay.dto.SoundPlayJournalDto;
import ru.medvedev.importer.soundplay.service.SoundPlayJournalService;

//@RestController
@RequiredArgsConstructor
//@RequestMapping("/soundux")
public class SoundPlayJournalController {

    private final SoundPlayJournalService service;

    @PostMapping("/playBegin")
    public ResponseEntity<Boolean> playBegin(@Validated SoundPlayJournalDto dto) {
        service.startSound(dto);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/playStop")
    public ResponseEntity<Boolean> playStop(@Validated SoundPlayJournalDto dto) {
        service.stopSound(dto);
        return ResponseEntity.ok(true);
    }
}
