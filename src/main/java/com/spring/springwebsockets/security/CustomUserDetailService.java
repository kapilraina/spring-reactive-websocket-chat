package com.spring.springwebsockets.security;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public class CustomUserDetailService implements ReactiveUserDetailsService {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    Map<String, UserDetails> users;
    PasswordEncoder passwordEncoder;

    public CustomUserDetailService(Map<String, UserDetails> users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public CustomUserDetailService() {
        super();
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        logger.info(" FindUserByName : " + username);
        return Mono.just(username)
                .flatMap(user -> getuserdetails(user))
                .onErrorResume(UsernameNotFoundException.class, e -> createUser(username))
                .map(ud -> {
                    users.put(ud.getUsername(),ud);
                    return ud;
                });

    }

    private Mono<UserDetails> createUser(String un) {
        User u = new User();
        u.setUsername(un);
        u.setPassword(passwordEncoder.encode("password"));
        u.setRoles(List.of("USER"));
        return Mono.just(new CustomUserDetails(u));

    }

    private Mono<UserDetails> getuserdetails(String user) {
        UserDetails userdetails = users.get(user);
        if (userdetails == null) {
            logger.info("User " + user + " Not Found");
            return Mono.error(new UsernameNotFoundException("User " + user + " Not Found"));
        } else {

            return Mono.just(userdetails);
        }
    }
}
