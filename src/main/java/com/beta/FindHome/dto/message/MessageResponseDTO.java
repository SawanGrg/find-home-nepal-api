package com.beta.FindHome.dto.message;

import com.beta.FindHome.enums.model.MessageStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private UUID id;
    private String senderName;
    private UUID conversationId;
    private String recipientName;
    private String content;
    private MessageStatus status;
    private LocalDateTime timeStamp;
}