package ru.medvedev.importer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.medvedev.importer.repository.UserRepository;
import ru.medvedev.importer.service.CustomAuthenticationProvider;
import ru.medvedev.importer.service.UserPermissionService;

import java.util.Collections;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserRepository userRepository;
    private final UserPermissionService userPermissionService;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        http.csrf().disable().requiresChannel(channel ->
                channel.anyRequest().requiresSecure())/*
                .authorizeRequests(authorize ->
                        authorize.anyRequest().permitAll())*/
                .authorizeRequests()
                .antMatchers("/contacts").hasAuthority("CONTACTS")
                .antMatchers("/xlsx/import").hasAuthority("DOWNLOAD_XLSX")
                .antMatchers("/file-storage").hasAuthority("FILE_STORAGE")
                .antMatchers("/events").hasAuthority("EVENTS")
                .antMatchers("/settings/fields").hasAuthority("COLUMN_NAME")
                .antMatchers("/settings/projects").hasAuthority("DOWNLOADS_PROJECT")
                .antMatchers("/settings/webhook-success-statuses").hasAuthority("WEBHOOK_STATUS")
                .antMatchers("/users").hasAuthority("USERS")
                .antMatchers("/webhook").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .defaultSuccessUrl("/xlsx/import", true)
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(new CustomAuthenticationProvider(
                userRepository, userPermissionService, passwordEncoder())));
    }
}
