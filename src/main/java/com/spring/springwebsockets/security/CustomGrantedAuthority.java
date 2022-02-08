package com.spring.springwebsockets.security;

import org.springframework.security.core.GrantedAuthority;

public class CustomGrantedAuthority implements GrantedAuthority {
    private String role;

    public CustomGrantedAuthority(String role) {
        this.role = role;
    }

    public CustomGrantedAuthority() {
        super();
    }

    @Override
    public String getAuthority() {
        return role;
    }
}
