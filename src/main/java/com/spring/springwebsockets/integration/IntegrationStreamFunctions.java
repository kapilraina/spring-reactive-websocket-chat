package com.spring.springwebsockets.integration;

import com.spring.springwebsockets.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Profile({"amqp","fuse","kafka"})
@Configuration
public class IntegrationStreamFunctions {
    @Autowired
    @Qualifier("pubfmc")
    FluxMessageChannel pubfmc;

    @Autowired
    @Qualifier("subfmc")
    FluxMessageChannel subfmc;

    @Bean
    public Supplier<Flux<Message<?>>> globalchatpubchannel() {
        return () -> {
            return Flux.from(pubfmc);
        };
    }

    @Bean
    public Consumer<Flux<Message<?>>> globalchatsubchannel() {
        return cFlux -> {

            cFlux.log().subscribe(m -> {
                System.out.println("cFlux -> "+ m.getHeaders().getTimestamp());
                subfmc.send(m);
            });
            // rabbitsubfmc.subscribeTo(cFlux);
        };
    }
}
