package com.beta.FindHome.events.publisher;

import com.beta.FindHome.events.event.ForgotPasswordOneTimePasswordEvent;
import com.beta.FindHome.events.event.LoginOneTimePasswordEvent;
import com.beta.FindHome.events.event.OwnerVerificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SMSEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public SMSEventPublisher(
            ApplicationEventPublisher applicationEventPublisher
    ){
        this.eventPublisher = applicationEventPublisher;
    }

    public void publishLoginOTPMessageEvent(
        String phoneNumber
    ){
        eventPublisher.publishEvent(new LoginOneTimePasswordEvent(this, phoneNumber));
    }

    public void publishForgotPasswordOTPMessageEvent(
            String phoneNumber
    ){
        eventPublisher.publishEvent(new ForgotPasswordOneTimePasswordEvent(this, phoneNumber));
    }
}
