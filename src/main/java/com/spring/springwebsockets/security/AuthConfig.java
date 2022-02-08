package com.spring.springwebsockets.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class AuthConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return new CustomUserDetailService(
                createSyntheticUsers(passwordEncoder()),
                passwordEncoder()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Map<String, UserDetails> createSyntheticUsers(
            PasswordEncoder passwordEncoder
    ) {
        return List
                .of(
                        new User(
                                "jim",
                                passwordEncoder.encode("password"),
                                List.of("USER", "SALES")
                        ),
                        new User(
                                "pam",
                                passwordEncoder.encode("password"),
                                List.of("USER", "ADMIN")
                        ),
                        new User(
                                "dwight",
                                passwordEncoder.encode("password"),
                                List.of("USER", "SALES", "DBA")
                        ),
                        new User(
                                "michael",
                                passwordEncoder.encode("password"),
                                List.of("USER", "MANAGER")
                        ),
                        new User(
                                "oscar",
                                passwordEncoder.encode("password"),
                                List.of("USER", "ACCOUNTANT")
                        ),
                        new User(
                                "angela",
                                passwordEncoder.encode("password"),
                                List.of("USER", "ACCOUNTANT")
                        ),
                        new User(
                                "kevin",
                                passwordEncoder.encode("password"),
                                List.of("USER", "KELVIN")
                        ),
                        new User(
                                "stanley",
                                passwordEncoder.encode("password"),
                                List.of("USER", "SALES")
                        ),
                        new User(
                                "phyllis",
                                passwordEncoder.encode("password"),
                                List.of("USER", "SALES")
                        ),
                        new User(
                                "creed",
                                passwordEncoder.encode("password"),
                                List.of("USER", "QA")
                        )
                )
                .stream()
                .map(u -> new CustomUserDetails(u))
                .collect(Collectors.toMap(CustomUserDetails::getUsername, cud -> cud));
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ChatUserRepository repo() {
        return new ChatUserRepository();
    }

}
