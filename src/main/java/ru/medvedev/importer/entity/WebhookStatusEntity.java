package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "webhook_status")
@Data
public class WebhookStatusEntity {

    @Id
    @SequenceGenerator(name = "webhook_status_gen_id", sequenceName = "webhook_status_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "webhook_status_gen_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name")
    private String name;
}
