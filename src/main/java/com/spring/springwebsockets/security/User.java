package com.spring.springwebsockets.security;

import lombok.*;
import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class User {

    private String username;
    private String password;
    private Collection<String> roles;
}
