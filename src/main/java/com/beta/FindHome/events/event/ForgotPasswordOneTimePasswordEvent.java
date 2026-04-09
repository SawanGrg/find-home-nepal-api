package com.beta.FindHome.events.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ForgotPasswordOneTimePasswordEvent extends ApplicationEvent {

    private String phoneNumber;

    public ForgotPasswordOneTimePasswordEvent(Object source, String phoneNumber) {
        super(source);
        this.phoneNumber = phoneNumber;
    }
}
