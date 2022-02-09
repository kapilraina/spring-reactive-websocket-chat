package com.spring.springwebsockets.sock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.springwebsockets.model.ChatMessage;
import com.spring.springwebsockets.model.ChatProps;
import com.spring.springwebsockets.model.MessageTypes;
import com.spring.springwebsockets.security.ChatUserRepository;
import com.spring.springwebsockets.utils.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks.Many;

@Configuration
public class SocksConfig {

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private ChatProps props;
    @Autowired
    private IntegrationFlowContext integrationFlowContext;

    @Bean(name = "wshbean8Chat")
    @Profile({"amqp", "fuse","kafka"})
    WebSocketHandler wshbean8Chat(
            ChatUtils utils,
            @Qualifier("pubfmc") FluxMessageChannel fmcpub,
            @Qualifier("subfmc") FluxMessageChannel fmcsub,
            ObjectMapper mapper,
            ChatUserRepository repo
    ) throws JsonProcessingException {
        return session -> {
            logger.info("[8] New Chat Session Initiated : " + session.getId());

            Flux<Message<ChatMessage>> incomingFlux = utils.prepareIncomingFlux(session, mapper, repo);
            fmcpub.subscribeTo(incomingFlux);

            Flux<WebSocketMessage> sessionOutboundFlux = utils.prepareOutBoundFluxFromRaw(session, fmcsub, mapper);

            Mono<Void> outbound = session.send(sessionOutboundFlux);
            return outbound;
        };
    }

    @Bean(name = "wshbean7Chat")
    @Profile({"localpubsub"})
    WebSocketHandler wshbean7Chat(
            ChatUtils utils,
            @Qualifier("pubfmc") FluxMessageChannel fmcpubsub,
            ObjectMapper mapper,
            ChatUserRepository repo
    )
            throws JsonProcessingException {
        return session -> {
            logger.info("[7] New Chat Session Initiated : " + session.getId());

            Flux<Message<ChatMessage>> incomingFlux = utils.prepareIncomingFlux(session, mapper, repo);
            fmcpubsub.subscribeTo(incomingFlux);

            Flux<WebSocketMessage> sessionOutboundFlux = utils.prepareOutBoundFlux(session, fmcpubsub, mapper);

            Mono<Void> outbound = session.send(sessionOutboundFlux);
            return outbound;
        };
    }

    @Bean(name = "wshbean6Chat")
    @Profile({"flowreg"})
    WebSocketHandler wshbean6Chat(
            ChatUtils utils,
            @Qualifier("pubfmc") FluxMessageChannel pubfmcreg,
            ObjectMapper mapper,
            ChatUserRepository repo
    )
            throws JsonProcessingException {
        return session -> {
            logger.info("[5] New Chat Session Initiated : " + session.getId());

            Flux<Message<ChatMessage>> incomingFlux = utils.prepareIncomingFlux(session, mapper, repo);
            IntegrationFlow standardIntegrationFlow = IntegrationFlows.from(((MessageChannel) pubfmcreg)).bridge().nullChannel();
            // StandardIntegrationFlow standardIntegrationFlow = IntegrationFlows.from(incomingFlux).channel(fmcpub).get();
            if (integrationFlowContext.getRegistrationById("globalChatIntegration") == null) {
                integrationFlowContext.registration(standardIntegrationFlow).register();
            }

            pubfmcreg.subscribeTo(incomingFlux);

            Flux<WebSocketMessage> sessionOutboundFlux = utils.prepareOutBoundFlux(session, pubfmcreg, mapper);
            Mono<Void> outbound = session.send(sessionOutboundFlux);
            return outbound;
        };
    }

    @Bean(name = "wshbean5Chat")
    @Profile({"sink"})
    WebSocketHandler wshbean5Chat(
            Many<ChatMessage> chatSessionStream,
            ChatUtils utils,
            ObjectMapper mapper,
            ChatUserRepository repo
    )
            throws JsonProcessingException {
        return session -> {
            logger.info("[4] New Chat Session Initiated : " + session.getId());

            Mono<Void> inbound = session
                    .receive()
                    .map(wsm -> wsm.getPayloadAsText())
                    .onErrorResume(
                            t -> {
                                logger.info("Chat Session Closed on Error: " + session.getId());
                                session.close();
                                return Mono.error(t);
                            }
                    )
                    .map(
                            s -> {
                                ChatMessage cmparsed = null;
                                try {
                                    cmparsed = mapper.readValue(s, ChatMessage.class);
                                    cmparsed.setTimestamp(utils.getCurrentTimeSamp());
                                    logger.info("Received :" + cmparsed);
                                    return cmparsed;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return cmparsed;
                            }
                    )
                    .map(cmsg -> utils.updateSessionRepo(repo, cmsg))
                    /*
                     * .flatMap(chatMessage -> {
                     * // Needed?
                     * if (chatMessage.getType().equals(MessageTypes.LEAVE)) {
                     * logger.info("Chat Session Closed on LEAVE message: " + session.getId());
                     * session.close();
                     * }
                     * return Mono.just(chatMessage);
                     * })
                     */
                    .doOnComplete(
                            () -> {
                                logger.info("Chat Session completed " + session.getId());
                                // session.close(); // No need?
                            }
                    )
                    // .log()
                    .doOnNext(chatSessionStream::tryEmitNext)
                    .then();

            Flux<WebSocketMessage> sessionOutboundFlux = chatSessionStream
                    .asFlux()
                    .map(
                            cmo1 -> {
                                if (cmo1.getType().equals(MessageTypes.LEAVE)) {
                                    cmo1.setMessage("Left");
                                }
                                return cmo1;
                            }
                    )
                    .map(
                            cmo -> {
                                try {
                                    return mapper.writeValueAsString(cmo);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                                return "";
                            }
                    )
                    .map(session::textMessage)
                    .onErrorResume(
                            t -> {
                                logger.info(
                                        t.getMessage() + "::Chat Session Closed : " + session.getId()
                                );
                                session.close();
                                return Mono.error(t);
                            }
                    );

            Mono<Void> outbound = session.send(sessionOutboundFlux);
            return Mono.zip(inbound, outbound).then();
        };
    }


    @Bean
    ChatUtils chatUtilsBean() {
        return new ChatUtils(props);
    }
}
