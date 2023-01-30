package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.*;

@Entity
@Table(name = "enabled_scenario")
@Data
public class EnabledScenarioEntity implements Cloneable {

    @Id
    @SequenceGenerator(name = "enabled_scenario_seq_gen", sequenceName = "enabled_scenario_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "enabled_scenario_seq_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;
}
