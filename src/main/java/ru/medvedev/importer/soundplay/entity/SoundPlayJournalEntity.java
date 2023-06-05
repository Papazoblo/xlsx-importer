package ru.medvedev.importer.soundplay.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sound_play_journal")
public class SoundPlayJournalEntity {

    @Id
    @SequenceGenerator(name = "soundPlayJournalIdGen", sequenceName = "sound_play_journal_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "soundPlayJournalIdGen", strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "request_id")
    private String requestId;
    @Column(name = "duration")
    private Long duration;
    @Column(name = "name")
    private String name;
    @Column(name = "utc_millis_start")
    private Long utcMillisStart;
    @Column(name = "utc_millis_stop")
    private Long utcMillisStop;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "create_at")
    private LocalDateTime createAt;
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
