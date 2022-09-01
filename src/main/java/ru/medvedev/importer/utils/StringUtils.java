package ru.medvedev.importer.utils;

import lombok.experimental.UtilityClass;
import ru.medvedev.importer.entity.ContactEntity;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class StringUtils {

    public static String transformTgMessage(String string) {
        return string.replaceAll("[_*]?(\\n)?", "");
    }

    public static String getFioStringFromContact(ContactEntity contact) {
        return String.format("%s %s %s", Optional.ofNullable(contact.getSurname()).orElse(""),
                Optional.ofNullable(contact.getName()).orElse(""),
                Optional.ofNullable(contact.getMiddleName()).orElse("")).trim();
    }

    public static String addPhoneCountryCode(String phone) {
        if (phone.length() == 10) {
            return "8" + phone;
        }
        return phone;
    }

    public static List<String> addPhoneCountryCode(List<String> phones) {
        return phones.stream()
                .map(StringUtils::addPhoneCountryCode)
                .collect(toList());
    }

    public static String replaceSpecialCharacters(String val) {
        return val.replaceAll("[+*_()#\\-\"'$№%^&? ,]+", "");
    }
}
