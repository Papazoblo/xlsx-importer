package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.SystemVariable;

import javax.persistence.*;

@Entity
@Table(name = "system_variable")
@Data
public class SystemVariableEntity {

    @Id
    @Column(name = "setting_name")
    @Enumerated(EnumType.STRING)
    private SystemVariable name;

    @Column(name = "setting_value")
    private String value;
}
