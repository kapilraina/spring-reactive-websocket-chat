package com.spring.springwebsockets.security;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.UserDetails;

import reactor.core.publisher.Mono;

public class ChatUserRepository {

    private Map<String, UserDetails> userRepo = new ConcurrentHashMap<String, UserDetails>();

    public Mono<UserDetails> newChatSession(UserDetails user) {
        userRepo.put(user.getUsername(), user);
        return Mono.just(user);
    }

    public Mono<Void> leftChatSession(String username) {
        userRepo.remove(username);
        return Mono.empty().then();
    }

    /*
     * public Flux<String> getActiveUsers() {
     * return Flux.fromIterable(userRepo.keySet());
     * }
     */

    public Set<String> getActiveUsers() {
        return userRepo.keySet();
    }

    public Mono<Boolean> userSessionExists(String username)
    {
        return userRepo.get(username==null?"":username)!=null ?Mono.just(true): Mono.just(false);
    }

}
