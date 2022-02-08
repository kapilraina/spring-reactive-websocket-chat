package com.spring.springwebsockets.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
@Service
public class CustomLoginSuccessHandler implements ServerAuthenticationSuccessHandler {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ChatUserRepository repo;
    public CustomLoginSuccessHandler(ChatUserRepository repo)
    {
        this.repo =repo;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        String username = ((UserDetails)authentication.getPrincipal()).getUsername();
        ServerWebExchange exchange = webFilterExchange.getExchange();
         return repo.userSessionExists(username)
                .flatMap(exists -> {
                    logger.info(username + " exists ? "+exists);
                    if(!exists) {
                        return webFilterExchange.getChain().filter(exchange);
                    }
                    else
                    {
                        exchange.getResponse().setStatusCode(HttpStatus.CONFLICT);
                        exchange.getResponse().setRawStatusCode(HttpStatus.CONFLICT.value());
                        return exchange
                                .getResponse()
                                .writeWith(
                                        Flux.just(
                                                exchange.getResponse().bufferFactory().wrap("Session Already Exists.".getBytes(StandardCharsets.UTF_8))));

                    }
                });

    }
}
