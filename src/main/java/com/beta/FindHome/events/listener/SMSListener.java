package com.beta.FindHome.events.listener;

import com.beta.FindHome.events.event.ForgotPasswordOneTimePasswordEvent;
import com.beta.FindHome.events.event.LoginOneTimePasswordEvent;
import com.beta.FindHome.utils.MessageServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SMSListener {

    private final MessageServiceUtils messageServiceUtils;

    @Autowired
    public SMSListener(MessageServiceUtils messageServiceUtils){
        this.messageServiceUtils = messageServiceUtils;
    }

    @Async
    @EventListener
    public void handleLoginOTPEvent(
            LoginOneTimePasswordEvent loginOneTimePasswordEvent
    ){
        messageServiceUtils.loginOTPSMS(loginOneTimePasswordEvent.getPhoneNumber());
    }

    @Async
    @EventListener
    public void handleForgetPasswordEvent(
            ForgotPasswordOneTimePasswordEvent forgotPasswordOneTimePasswordEvent
    ){
        messageServiceUtils.forgotPasswordOTPSMS(forgotPasswordOneTimePasswordEvent.getPhoneNumber());
    }
}
