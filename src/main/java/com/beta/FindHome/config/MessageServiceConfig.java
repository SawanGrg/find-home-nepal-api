package com.beta.FindHome.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class MessageServiceConfig {

    @Value("${MESSAGE_API_TOKEN}")
    private String token;

    @Value("${MESSAGE_API_URL}")
    private String messageAPIURL;

    public String getMessageAPIToken() {
        return token;
    }

    public String getMessageAPIURL() {
        return messageAPIURL;
    }
}
