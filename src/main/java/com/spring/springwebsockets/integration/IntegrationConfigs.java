package com.spring.springwebsockets.integration;

import com.spring.springwebsockets.model.ChatMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Configuration
public class IntegrationConfigs {

    @Bean
    @Profile({"sink"})
    Many<ChatMessage> chatMessageStream() {
        return Sinks.many().multicast().<ChatMessage>onBackpressureBuffer();
    }

    @Bean
    @Qualifier("pubfmc")
    @Profile({"!sink"})
    FluxMessageChannel pubfmc() {
        FluxMessageChannel fmc = new FluxMessageChannel();
        return fmc;
    }


    @Bean
    @Qualifier("subfmc")
    @Profile({"amqp","fuse","kafka"})
    FluxMessageChannel subfmc() {
        FluxMessageChannel fmc = new FluxMessageChannel();
        return fmc;
    }

/*    @Bean
    @Qualifier("pubsubfmc")
    @Profile({"localpubsub"})
    FluxMessageChannel pubsubfmc() {
        FluxMessageChannel fmc = new FluxMessageChannel();
        return fmc;
    }*/

    @Bean
    @Profile("fuse")
    IntegrationFlow fluxItegration(@Qualifier("pubfmc") FluxMessageChannel fmcin, @Qualifier("subfmc") FluxMessageChannel fmcout) {
        return IntegrationFlows.from(((MessageChannel) fmcin))
                .channel(fmcout).get();

    }

/*    @Bean
    IntegrationFlow fluxItegration(@Qualifier("pubfmc") FluxMessageChannel pubfmc) {
        return IntegrationFlows.from(((MessageChannel) pubfmc)).bridge().nullChannel();
    }

    @Bean
    IntegrationFlow fluxItegration2(@Qualifier("subfmc") FluxMessageChannel subfmc) {
        return IntegrationFlows.from(((MessageChannel) subfmc)).bridge().nullChannel();
    }*/
}
