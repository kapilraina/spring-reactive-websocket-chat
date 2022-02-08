package com.spring.springwebsockets.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "chatprops")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatProps {

    private String timestampdateformat;

}
