package com.spring.springwebsockets.security;

import jdk.jfr.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
public class ExistingAuthPresenceFilter implements WebFilter {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private ChatUserRepository repo;

    public ExistingAuthPresenceFilter(ChatUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
        return formData.flatMap(mvMap -> {
            String username = mvMap.getFirst("username");
            if (username == null)
                return chain.filter(exchange);
            return repo.userSessionExists(username)
                    .flatMap(exists -> {
                        if (!exists) {
                            logger.info("ExistingAuthPresenceFilter - Session Does not Exists for " + username);
                            return chain.filter(exchange);
                        } else {
                            logger.info("Session Already Exists for " + username);
                            // exchange.getResponse().setStatusCode(HttpStatus.);
                            // exchange.getResponse().setRawStatusCode(HttpStatus.CONFLICT.value());
                            exchange.getResponse().getHeaders().add("Content-Type", MimeTypeUtils.TEXT_HTML_VALUE);
                            return exchange
                                    .getResponse()
                                    .writeWith(
                                            Flux.just(
                                                    exchange
                                                            .getResponse().bufferFactory()
                                                            .wrap(
                                                                    String.format(
                                                                            "<p>Session Already Exists for %s. Logoff From that session first."
                                                                                    +
                                                                                    "<a href='chat.html'>Main</a></p>",
                                                                            username)
                                                                            .getBytes(StandardCharsets.UTF_8))));

                        }
                    });
        });
    }
}
