package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

@Data
public class RaiffeisenCreateLeadInfoDto {

    private String city;
    private String inn;
    private String companyName;
    private RaiffeisenPersonDto personName;
    private RaiffeisenCommunicationChannelsDto communicationChannels;
}
