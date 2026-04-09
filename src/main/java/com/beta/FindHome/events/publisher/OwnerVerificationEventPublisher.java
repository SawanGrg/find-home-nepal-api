package com.beta.FindHome.events.publisher;

import com.beta.FindHome.events.event.OwnerVerificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OwnerVerificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public OwnerVerificationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher
    ){
        this.eventPublisher = applicationEventPublisher;
    }

    public void publishOwnerVerificationMessageEvent(
            String message,
            String phoneNumber
    ) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        eventPublisher.publishEvent(new OwnerVerificationEvent(this, message, phoneNumber));
    }

}
