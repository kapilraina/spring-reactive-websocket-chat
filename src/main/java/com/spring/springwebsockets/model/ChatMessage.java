package com.spring.springwebsockets.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChatMessage {
    private String username;
    private String message;
    private String timestamp;
    private MessageTypes type;

}
