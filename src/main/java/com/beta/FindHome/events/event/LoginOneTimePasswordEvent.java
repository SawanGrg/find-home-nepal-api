package com.beta.FindHome.events.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginOneTimePasswordEvent extends ApplicationEvent {

    private String phoneNumber;

    public LoginOneTimePasswordEvent(Object source, String phoneNumber) {
        super(source);
        this.phoneNumber = phoneNumber;
    }
}
