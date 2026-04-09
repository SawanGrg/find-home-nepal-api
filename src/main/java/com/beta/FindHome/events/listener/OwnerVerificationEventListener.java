package com.beta.FindHome.events.listener;

import com.beta.FindHome.events.event.OwnerVerificationEvent;
import com.beta.FindHome.utils.MessageServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OwnerVerificationEventListener {

    private MessageServiceUtils messageServiceUtils;

    OwnerVerificationEventListener(
            MessageServiceUtils messageServiceUtils
    ){
        this.messageServiceUtils = messageServiceUtils;
    }

    @Async
    @EventListener
    public void handleOwnerVerificationEvent(OwnerVerificationEvent event) {
        try {
            messageServiceUtils.ownerVerificationSMS(
                    event.getPhoneNumber(),
                    event.getMessage()
            );
        } catch (Exception e) {
            log.error("Failed to send verification SMS to {}", event.getPhoneNumber(), e);
            // Consider adding retry logic here or dead-letter queue
        }
    }
}
