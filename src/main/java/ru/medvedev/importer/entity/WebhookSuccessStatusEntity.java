package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "webhook_success_status")
@Data
public class WebhookSuccessStatusEntity {

    @Id
    @SequenceGenerator(name = "webhook_success_status_id_gen", sequenceName = "webhook_success_status_seq_id",
            allocationSize = 1)
    @GeneratedValue(generator = "webhook_success_status_id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name")
    private String name;
}
