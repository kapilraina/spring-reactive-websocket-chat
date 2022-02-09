package com.spring.springwebsockets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.spring.springwebsockets.sock.SimpleSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@SpringBootApplication
@RestController
public class SpringreactivewebsocketchatApplication {

  Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  public static void main(String[] args) {
    //System.setProperty("spring.profiles.active", "wsserver");
    SpringApplication.run(SpringreactivewebsocketchatApplication.class, args);
  }

  @Bean
  ApplicationRunner ar(@Qualifier("subfmc") FluxMessageChannel fmcsub)
  {
    return args ->{
      fmcsub.subscribe(new SimpleSubscriber());
    };
  }

}
