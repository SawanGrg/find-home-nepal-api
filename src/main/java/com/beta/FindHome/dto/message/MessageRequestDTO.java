package com.beta.FindHome.dto.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {
    private String senderName;
    private String recipientName;
    private UUID conversationId;
    private String content;

    @JsonCreator
    public MessageRequestDTO(
            @JsonProperty("conversationId") UUID conversationId,
            @JsonProperty("senderName") String senderName,
            @JsonProperty("recipientName") String recipientName,
            @JsonProperty("content") String content) {
        this.conversationId = conversationId;
        this.senderName = senderName;
        this.recipientName = recipientName;
        this.content = content;
    }
}