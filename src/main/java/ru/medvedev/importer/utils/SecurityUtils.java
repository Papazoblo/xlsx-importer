package ru.medvedev.importer.utils;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class SecurityUtils {

    public static List<String> getAuthorityList() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(toList());
    }
}
