package com.spring.springwebsockets.integration;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

@Profile({ "amqp", "kafka" })
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
      cFlux
        .log()
        .subscribe(
          m -> {
            subfmc.send(m);
          }
        );
      // rabbitsubfmc.subscribeTo(cFlux);
    };
  }
}
