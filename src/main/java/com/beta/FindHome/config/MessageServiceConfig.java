package com.beta.FindHome.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class MessageServiceConfig {

    @Value("${MESSAGE_API_TOKEN_KEY}")
    private String token;

    @Value("${MESSAGE_API_URL_KEY}")
    private String messageAPIURL;

    public String getMessageAPIToken() {
        return token;
    }

    public String getMessageAPIURL() {
        return messageAPIURL;
    }
}
