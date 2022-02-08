package com.spring.springwebsockets.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.springwebsockets.model.ChatMessage;
import com.spring.springwebsockets.model.ChatProps;
import com.spring.springwebsockets.model.MessageTypes;
import com.spring.springwebsockets.security.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatUtils {

    private ChatProps props;
    SimpleDateFormat sdf = null;
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ChatUtils(ChatProps props) {
        this.props = props;
        sdf = new SimpleDateFormat(props.getTimestampdateformat());
    }

    public String getCurrentTimeSamp() {
        return sdf.format(new Date());
    }


    public Flux<Message<ChatMessage>> prepareIncomingFlux(
            WebSocketSession session,
            ObjectMapper mapper,
            ChatUserRepository repo


    ) {
        return session.receive()
                .map(wsm -> wsm.getPayloadAsText())
                .onErrorResume(
                        t -> {
                            logger.info("Chat Session Closed on Error: " + t);
                            session.close();
                            return Mono.error(t);
                        }
                )
                .map(
                        s -> {
                            ChatMessage cmparsed = null;
                            try {
                                cmparsed = mapper.readValue(s, ChatMessage.class);
                                cmparsed.setTimestamp(getCurrentTimeSamp());
                                logger.info("Received :" + cmparsed);
                                return cmparsed;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return Mono.error(e);
                            }
                        }
                )
                .map(cmsg -> updateSessionRepo(repo, (ChatMessage) cmsg))
                .map(cmsg -> MessageBuilder.withPayload(cmsg).setHeader("webSocketSession", session).build())

                .doOnComplete(
                        () -> {
                            logger.info("Chat Session completed " + session.getId());
                            // session.close(); // No need?
                        }
                );

    }

    public Flux<WebSocketMessage> prepareOutBoundFluxFromRaw(
            WebSocketSession session,
            FluxMessageChannel fmcpubsub,
            ObjectMapper mapper
    ) {

        return Flux.from(fmcpubsub)
                .map(m -> {
                    ChatMessage cm = null;
                    try {
                        if(m.getPayload() instanceof ChatMessage)
                        {
                            cm =  (ChatMessage) m.getPayload();
                        }
                        else {
                            cm = mapper.readValue(new String((byte[]) m.getPayload()), ChatMessage.class);
                        }
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return Mono.error(e);
                    }
                    return cm;
                })
                .map(
                        (cmo1) -> {
                            if (((ChatMessage) cmo1).getType().equals(MessageTypes.LEAVE)) {
                                ((ChatMessage) cmo1).setMessage("Left");
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
    }


    public Flux<WebSocketMessage> prepareOutBoundFlux(
            WebSocketSession session,
            FluxMessageChannel fmcpubsub,
            ObjectMapper mapper
    ) {
        return Flux.from(fmcpubsub)
                .map(cmsg -> (ChatMessage) cmsg.getPayload())
                .log()
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
                                return Mono.error(e);
                            }

                        }
                )
                .map(cmop -> session.textMessage((String) cmop))
                .onErrorResume(
                        t -> {
                            logger.info(
                                    t.getMessage() + "::Chat Session Closed : " + session.getId()
                            );
                            session.close();
                            return Mono.error(t);
                        }
                );
    }

    public ChatMessage updateSessionRepo(
            ChatUserRepository repo,
            ChatMessage cmsg
    ) {
        if (cmsg.getType().equals(MessageTypes.LEAVE)) {
            repo.leftChatSession(cmsg.getUsername());
            System.out.println("Session Repo Updated for " + cmsg);
        }

        return cmsg;
    }

}
