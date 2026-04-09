package com.beta.FindHome.dto.message.initiate;

import com.beta.FindHome.enums.model.ConversationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiateChatResponseDTO {
    public ConversationType conversationType;
    public UUID conversationId;
    public String userName;            // Changed from userId
    public String propertyOwnerName;   // Changed from propertyOwnerId
    public LocalDateTime timeStamp;
}