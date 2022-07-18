package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.UserEntity;
import ru.medvedev.importer.repository.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository repository;
    private final UserPermissionService userPermissionService;
    private final PasswordEncoder passwordEncoder;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal() + "";
        String password = authentication.getCredentials() + "";

        Optional<UserEntity> userOptional = repository.findByLogin(username);
        if (!userOptional.isPresent()) {
            throw new BadCredentialsException("Пользователь не найден");
        }
        userOptional.ifPresent(user -> {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Логин или пароль неверны");
            }
            if (!user.getActive()) {
                throw new DisabledException("Пользователь заблокирован");
            }
        });
        return new UsernamePasswordAuthenticationToken(username, null,
                userPermissionService.getAll(userOptional.get().getId()).stream()
                        .map(permission -> (GrantedAuthority) permission::name)
                        .collect(Collectors.toList()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
