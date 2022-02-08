package com.spring.springwebsockets.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private String username;
    private String password;
    private Collection<GrantedAuthority> gAuthorities;

    public CustomUserDetails() {
        super();
    }

    public CustomUserDetails(User user) {

        gAuthorities = new ArrayList<GrantedAuthority>();
        username = user.getUsername();
        password = user.getPassword();
        user.getRoles().stream()
                .map(role -> new CustomGrantedAuthority("ROLE_" + role))
                .forEach(ga -> gAuthorities.add(ga));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return gAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", getAuthorities=" + gAuthorities +
                '}';
    }
}
