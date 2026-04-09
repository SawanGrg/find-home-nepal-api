package com.beta.FindHome.controller.chat;

import com.beta.FindHome.dto.message.MessageRequestDTO;
import com.beta.FindHome.dto.message.MessageResponseDTO;
import com.beta.FindHome.dto.message.initiate.InitiateChatResponseDTO;
import com.beta.FindHome.enums.model.MessageStatus;
import com.beta.FindHome.service.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @Autowired
    public ChatController(
            SimpMessagingTemplate messagingTemplate,
            ChatService chatServiceImpl
    ) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatServiceImpl;
    }

    // Get all messages of a conversation by conversation ID
    @GetMapping("/message/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessageList(
            @PathVariable(name = "conversationId", required = true) UUID conversationId
    ) {
        try {
            List<MessageResponseDTO> messages = chatService.getMessagesList(conversationId);

            // Only send WebSocket notification if there are messages
            if (!messages.isEmpty()) {
                messagingTemplate.convertAndSend(
                        "/topic/conversation-" + conversationId,
                        messages
                );
            }

            return ResponseEntity.ok(messages);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

//    GET all conversations id (--users--) of a user by user ID
    @GetMapping("/conversation/{userName}")
    public ResponseEntity<List<InitiateChatResponseDTO>> getConversations(
            @PathVariable(name = "userName", required = true) String userName
    ){
        try{
            List<InitiateChatResponseDTO> conversations = chatService.getConversations(userName);
            return ResponseEntity.ok(conversations);
        }catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // user initiates a conversation with a property owner
    // user will get the conversation ID and the property owner will be notified
    @GetMapping("/initiate/{userName}/{propertyId}")
    public ResponseEntity<InitiateChatResponseDTO> initiateConversation(
            @PathVariable(name = "userName", required = true) String userName,
            @PathVariable(name = "propertyId", required = true) UUID propertyId
    ){
        try{
            InitiateChatResponseDTO response = this.chatService.checkConversationBetweenUsers(
                    userName,
                    propertyId
            );

            messagingTemplate.convertAndSend(
                    "/topic/conversation-notify-" + response.getPropertyOwnerName(), // Changed to propertyOwnerName
                    response.getConversationId()
            );
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @MessageMapping("/send-message")
    public ResponseEntity<MessageResponseDTO> sendMessage(MessageRequestDTO request) {
        try {
            if (request == null || request.getContent() == null || request.getContent().isBlank()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }

            MessageResponseDTO messageResponseDTO = chatService.sendMessage(request);

            messagingTemplate.convertAndSend(
                    "/topic/conversation-" + request.getConversationId(),
                    messageResponseDTO
            );

            messagingTemplate.convertAndSend(
                    "/queue/confirmations-" + messageResponseDTO.getSenderName(),  // Changed to senderName
                    "Message delivered to conversation " + MessageStatus.DELIVERED
            );

            return ResponseEntity.ok(messageResponseDTO);
        } catch (Exception ex) {
            ex.printStackTrace();
            messagingTemplate.convertAndSend(
                    "/queue/confirmations-" + request.getSenderName(),  // Changed to senderName
                    "Message delivery failed: " + MessageStatus.ERROR
            );
            return ResponseEntity.badRequest().build();
        }
    }
}