package com.beta.FindHome.service.chat;

import com.beta.FindHome.dto.message.MessageRequestDTO;
import com.beta.FindHome.dto.message.MessageResponseDTO;
import com.beta.FindHome.dto.message.initiate.InitiateChatResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * ChatService interface provides methods for sending messages, retrieving message lists,
 * and checking access to conversations.
 */
public interface ChatService {
    InitiateChatResponseDTO checkConversationBetweenUsers(String userName, UUID propertyId);
    MessageResponseDTO sendMessage(MessageRequestDTO request);
    List<MessageResponseDTO> getMessagesList(UUID conversationId);
    List<InitiateChatResponseDTO> getConversations(String userName);
    boolean hasAccessToConversation(UUID conversationId, UUID userId);
}
