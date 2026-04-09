package com.beta.FindHome.events.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OwnerVerificationEvent extends ApplicationEvent {
    private String message;
    private String phoneNumber;

    public OwnerVerificationEvent(
            Object source,
            String message,
            String phoneNumber
    ) {
        super(source);
        this.message = message;
        this.phoneNumber = phoneNumber;
    }
}
