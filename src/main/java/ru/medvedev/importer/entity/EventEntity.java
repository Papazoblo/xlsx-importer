package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.EventType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "event")
@Entity
@Data
public class EventEntity {

    @Id
    @SequenceGenerator(name = "eventSeqId", sequenceName = "event_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "eventSeqId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private EventType type;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "description")
    private String description;

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
    }
}
