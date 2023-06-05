package ru.medvedev.importer.soundplay.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SoundPlayJournalDto {

    @NotNull
    private String id;
    private Long duration;
    private String name;
    private Long utcmillis;
    private String userid;
}
