package ru.medvedev.importer.dto.alfa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactInfoDto {
    private String phoneNumber;
    private String fullName;
    private String contactEmail;
    private String contractorsPosition;
    private String extraPhoneNumbers;

    public static ContactInfoDto of(String phone) {
        ContactInfoDto dto = new ContactInfoDto();
        dto.setPhoneNumber(phone);
        return dto;
    }
}
