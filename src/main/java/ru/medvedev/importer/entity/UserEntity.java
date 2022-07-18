package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @SequenceGenerator(name = "user_seq_id_gen", sequenceName = "user_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "user_seq_id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "fio")
    private String fio;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
    }
}
