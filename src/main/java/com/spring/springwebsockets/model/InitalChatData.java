package com.spring.springwebsockets.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InitalChatData {

    private String currentUsername;
    // private Flux<String> activeUsers;
    private Set<String> activeUsers;
    private String randomvector;

}
