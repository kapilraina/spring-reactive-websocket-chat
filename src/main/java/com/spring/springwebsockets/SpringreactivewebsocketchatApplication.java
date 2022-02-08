package com.spring.springwebsockets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
  /**
   * low latency, high frequency, and high volume that make the best case for the
   * use of WebSocket.
   *
   * @return
   */

}
