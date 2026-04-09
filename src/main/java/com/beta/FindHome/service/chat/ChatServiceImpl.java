package com.beta.FindHome.service.chat;

import com.beta.FindHome.dto.message.*;
import com.beta.FindHome.dto.message.initiate.InitiateChatResponseDTO;
import com.beta.FindHome.enums.model.ConversationType;
import com.beta.FindHome.enums.model.MessageStatus;
import com.beta.FindHome.exception.*;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.ConversationRepository;
import com.beta.FindHome.repository.MessageRepository;
import com.beta.FindHome.repository.ParticipantRepository;
import com.beta.FindHome.repository.UserRepository;
import com.beta.FindHome.service.property.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final PropertyService propertyService;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatServiceImpl(
            PropertyService propertyService,
            ConversationRepository conversationRepository,
            ParticipantRepository participantRepository,
            MessageRepository messageRepository,
            UserRepository userRepository) {
        this.propertyService = propertyService;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // =====================================================================
    // INITIATE CHAT
    // =====================================================================

    @Override
    @Transactional
    public InitiateChatResponseDTO checkConversationBetweenUsers(
            String userName,
            UUID propertyId
    ) {
        Users landlord = propertyService.findLandLordIdByPropertyId(propertyId);
        if (landlord == null) {
            throw new ResourceNotFoundException(
                    "Property owner not found for property ID: " + propertyId);
        }

        Users user = userRepository.findByUserNameWithRoles(userName)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + userName));

        // Prevent landlord from chatting with themselves
        boolean sameRole = user.getRoles().stream()
                .anyMatch(r -> landlord.getRoles().stream()
                        .anyMatch(lr -> lr.getRoleName().equals(r.getRoleName())));
        if (sameRole) {
            throw new UserException("User and landlord cannot have the same role.");
        }

        List<Conversation> existing = conversationRepository
                .findConversationsBetweenUsers(userName, landlord.getUserName());

        if (!existing.isEmpty()) {
            return buildInitiateResponse(existing.get(0), userName, landlord.getUserName());
        }

        return createNewConversation(user, landlord);
    }

    // =====================================================================
    // SEND MESSAGE
    // =====================================================================

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(MessageRequestDTO request) {
        Conversation conversation = conversationRepository
                .findByIdOrThrow(request.getConversationId());

        Users sender = userRepository.findByUserNameWithRoles(request.getSenderName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + request.getSenderName()));

        validateParticipantAccess(conversation.getId(), sender.getId());

        Message message = createAndSaveMessage(request, conversation, sender);
        conversationRepository.save(conversation); // triggers @LastModifiedDate

        return buildMessageResponse(message, request.getRecipientName());
    }

    // =====================================================================
    // GET MESSAGES
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getMessagesList(UUID conversationId) {
        Conversation conversation = conversationRepository.findByIdOrThrow(conversationId);

        Set<Participant> participants = conversation.getParticipants();
        if (participants.size() != 2) {
            throw new IllegalStateException(
                    "Private conversation must have exactly 2 participants");
        }

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(message -> {
                    String recipientName = participants.stream()
                            .map(Participant::getUser)
                            .filter(u -> !u.getUserName().equals(
                                    message.getSender().getUserName()))
                            .map(Users::getUserName)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "Recipient not found in conversation"));
                    return buildMessageResponse(message, recipientName);
                })
                .collect(Collectors.toList());
    }

    // =====================================================================
    // GET CONVERSATIONS
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<InitiateChatResponseDTO> getConversations(String userName) {
        Users user = userRepository.findByUserNameWithRoles(userName)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + userName));

        return participantRepository.findByUser_Id(user.getId())
                .stream()
                .map(Participant::getConversation)
                .map(conversation -> {
                    Set<Participant> participants = conversation.getParticipants();
                    if (participants.size() != 2) {
                        throw new IllegalStateException(
                                "Private conversation must have exactly 2 participants");
                    }

                    String otherUsername = participants.stream()
                            .map(Participant::getUser)
                            .filter(u -> !u.getId().equals(user.getId()))
                            .map(Users::getUserName)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "Other participant not found"));

                    return InitiateChatResponseDTO.builder()
                            .conversationId(conversation.getId())
                            .userName(user.getUserName())
                            .propertyOwnerName(otherUsername)
                            .conversationType(conversation.getType())
                            .timeStamp(conversation.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // =====================================================================
    // ACCESS CHECK
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToConversation(UUID conversationId, UUID userId) {
        return participantRepository.existsByConversation_IdAndUser_Id(
                conversationId, userId);
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    private InitiateChatResponseDTO createNewConversation(
            Users user,
            Users landlord
    ) {
        Conversation conversation = conversationRepository.save(
                Conversation.builder()
                        .type(ConversationType.PRIVATE)
                        .build()
        );

        participantRepository.save(Participant.builder()
                .user(user)
                .conversation(conversation)
                .build());

        participantRepository.save(Participant.builder()
                .user(landlord)
                .conversation(conversation)
                .build());

        return buildInitiateResponse(
                conversation,
                user.getUserName(),
                landlord.getUserName()
        );
    }

    private Message createAndSaveMessage(
            MessageRequestDTO request,
            Conversation conversation,
            Users sender
    ) {
        return messageRepository.save(
                Message.builder()
                        .conversation(conversation)
                        .sender(sender)
                        .content(request.getContent())
                        .status(MessageStatus.SENT)
                        .build()
        );
    }

    private void validateParticipantAccess(UUID conversationId, UUID userId) {
        if (!participantRepository.existsByConversation_IdAndUser_Id(
                conversationId, userId)) {
            throw new ParticipantAccessException(
                    "User does not have access to this conversation");
        }
    }

    private InitiateChatResponseDTO buildInitiateResponse(
            Conversation conversation,
            String userName,
            String propertyOwnerName
    ) {
        return InitiateChatResponseDTO.builder()
                .conversationId(conversation.getId())
                .userName(userName)
                .propertyOwnerName(propertyOwnerName)
                .conversationType(conversation.getType())
                .timeStamp(conversation.getUpdatedAt())
                .build();
    }

    private MessageResponseDTO buildMessageResponse(
            Message message,
            String recipientName
    ) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderName(message.getSender().getUserName())
                .recipientName(recipientName)
                .content(message.getContent())
                .status(message.getStatus())
                .timeStamp(message.getCreatedAt())
                .build();
    }
}

//package com.beta.FindHome.service.chat;
//
//import com.beta.FindHome.dao.ConversationDAO;
//import com.beta.FindHome.dao.MessageDAO;
//import com.beta.FindHome.dao.ParticipantDAO;
//import com.beta.FindHome.dto.message.*;
//import com.beta.FindHome.dto.message.initiate.InitiateChatResponseDTO;
//import com.beta.FindHome.enums.model.ConversationType;
//import com.beta.FindHome.enums.model.MessageStatus;
//import com.beta.FindHome.exception.*;
//import com.beta.FindHome.model.*;
//import com.beta.FindHome.repository.UserRepository;
//import com.beta.FindHome.service.property.PropertyService;
//import com.beta.FindHome.service.user.UsersService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//public class ChatServiceImpl implements ChatService {
//
//    private final PropertyService propertyService;
//    private final UsersService userService;
//    private final ConversationDAO conversationDAOImpl;
//    private final ParticipantDAO participantDAOImpl;
//    private final MessageDAO messageDAOImpl;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public ChatServiceImpl(
//            PropertyService propertyService,
//            UsersService userService,
//            ConversationDAO conversationDAOImpl,
//            ParticipantDAO participantDAOImpl,
//            MessageDAO messageDAOImpl,
//            UserRepository userRepository) {
//        this.propertyService = propertyService;c
//        this.userService = userService;
//        this.conversationDAOImpl = conversationDAOImpl;
//        this.participantDAOImpl = participantDAOImpl;
//        this.messageDAOImpl = messageDAOImpl;
//        this.userRepository = userRepository;
//    }
//
//    @Transactional
//    public InitiateChatResponseDTO checkConversationBetweenUsers(String userName, UUID propertyId) {
//        Users landlord = propertyService.findLandLordIdByPropertyId(propertyId);
//        if (landlord == null) {
//            throw new UserException("Property owner not found for property ID: " + propertyId);
//        }
//        Users user = (Users) userService.loadUserByUsername(userName);
//        if (user == null) {
//            throw new UsernameNotFoundException("User with username " + userName + " not found while initiating chat.");
//        }
//        //if the user role and landlord role is same then throw exception
//        if (user.getRoles().iterator().next().getId()
//                .equals(landlord.getRoles().iterator().next().getId())) {
//            throw new UserException("User and landlord cannot have the same role.");
//        }
//        List<Conversation> existingConversations = conversationDAOImpl.findConversationsBetweenUsers(
//                userName,
//                landlord.getUsername()
//        );
//        if (!existingConversations.isEmpty()) {
//            return buildInitiateResponse(existingConversations.get(0), userName, landlord.getUsername());
//        }
//        return createNewConversation(userName, landlord.getUsername());
//    }
//
//    @Transactional
//    public MessageResponseDTO sendMessage(MessageRequestDTO request) {
//        Conversation conversation = conversationDAOImpl.findById(request.getConversationId());
//        if (conversation == null) {
//            throw new ConversationNotFoundException("Conversation not found with ID: " + request.getConversationId());
//        }
//        Users sender = (Users) userService.loadUserByUsername(request.getSenderName());
//        if (sender == null) {
//            throw new UserException("User not found with username: " + request.getSenderName());
//        }
//        validateParticipantAccess(conversation.getId(), sender.getId());
//
//        Message message = createAndSaveMessage(request, conversation, sender);
//        updateConversationTimestamp(conversation);
//
//        return buildMessageResponse(message, request.getRecipientName());
//    }
//
//    @Transactional(readOnly = true)
//    public List<MessageResponseDTO> getMessagesList(UUID conversationId) {
//        Conversation conversation = conversationDAOImpl.findById(conversationId);
//        if (conversation == null) {
//            throw new ConversationNotFoundException("Conversation not found with ID: " + conversationId);
//        }
//
//        Set<Participant> participants = conversation.getParticipants();
//        if (participants.size() != 2) {
//            throw new IllegalStateException("Private conversation should have exactly 2 participants");
//        }
//
//        return messageDAOImpl.findByConversationId(conversationId).stream()
//                .map(message -> {
//                    String recipientName = participants.stream()
//                            .map(Participant::getUser)
//                            .filter(user -> !user.getUsername().equals(message.getSender().getUsername()))
//                            .map(Users::getUsername)
//                            .findFirst()
//                            .orElseThrow(() -> new IllegalStateException("Recipient not found"));
//
//                    return buildMessageResponse(message, recipientName);
//                })
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<InitiateChatResponseDTO> getConversations(String userName) {
//        Users user = (Users) userService.loadUserByUsername(userName);
//        if (user == null) {
//            throw new UserException("User not found with username: " + userName);
//        }
//
//        return participantDAOImpl.findByUserId(user.getId()).stream()
//                .map(Participant::getConversation)
//                .map(conversation -> {
//                    Set<Participant> participants = conversation.getParticipants();
//                    if (participants.size() != 2) {
//                        throw new IllegalStateException("Private conversation should have exactly 2 participants");
//                    }
//
//                    String otherParticipantUsername = participants.stream()
//                            .map(Participant::getUser)
//                            .filter(u -> !u.getId().equals(user.getId()))
//                            .map(Users::getUsername)
//                            .findFirst()
//                            .orElseThrow(() -> new IllegalStateException("Other participant not found"));
//
//                    return InitiateChatResponseDTO.builder()
//                            .conversationId(conversation.getId())
//                            .userName(user.getUsername())
//                            .propertyOwnerName(otherParticipantUsername)
//                            .conversationType(conversation.getType())
//                            .timeStamp(conversation.getUpdatedAt())
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public boolean hasAccessToConversation(UUID conversationId, UUID userId) {
//        return participantDAOImpl.existsByConversationIdAndUserId(conversationId, userId);
//    }
//
//    private InitiateChatResponseDTO createNewConversation(String userName, String propertyOwnerName) {
//        Users user = (Users) userService.loadUserByUsername(userName);
//        Users landlord = userRepository.findByUserName(propertyOwnerName);
//        if (user == null || landlord == null) {
//            throw new UserException("User not found with username: " + userName);
//        }
//
//        Conversation newConversation = Conversation.builder()
//                .type(ConversationType.PRIVATE)
//                .build();
//        newConversation.setCreatedAt(LocalDateTime.now());
//        newConversation.setUpdatedAt(LocalDateTime.now());
//        newConversation = conversationDAOImpl.save(newConversation);
//
//        createParticipant(newConversation, user);
//        createParticipant(newConversation, landlord);
//
//        return buildInitiateResponse(newConversation, userName, propertyOwnerName);
//    }
//
//    private void createParticipant(Conversation conversation, Users user) {
//        Participant participant = Participant.builder()
//                .user(user)
//                .conversation(conversation)
//                .build();
//        participant.setCreatedAt(LocalDateTime.now());
//        participantDAOImpl.save(participant);
//    }
//
//    private Message createAndSaveMessage(MessageRequestDTO request, Conversation conversation, Users sender) {
//        Message message = Message.builder()
//                .conversation(conversation)
//                .sender(sender)
//                .content(request.getContent())
//                .status(MessageStatus.SENT)
//                .build();
//        message.setCreatedAt(LocalDateTime.now());
//        message.setUpdatedAt(LocalDateTime.now());
//        return messageDAOImpl.save(message);
//    }
//
//    private void updateConversationTimestamp(Conversation conversation) {
//        conversation.setUpdatedAt(LocalDateTime.now());
//        conversationDAOImpl.save(conversation);
//    }
//
//    private void validateParticipantAccess(UUID conversationId, UUID userId) {
//        if (!participantDAOImpl.existsByConversationIdAndUserId(conversationId, userId)) {
//            throw new ParticipantAccessException("User doesn't have access to this conversation");
//        }
//    }
//
//    private InitiateChatResponseDTO buildInitiateResponse(Conversation conversation, String userName, String propertyOwnerName) {
//        return InitiateChatResponseDTO.builder()
//                .conversationId(conversation.getId())
//                .userName(userName)
//                .propertyOwnerName(propertyOwnerName)
//                .conversationType(conversation.getType())
//                .timeStamp(conversation.getUpdatedAt())
//                .build();
//    }
//
//    private MessageResponseDTO buildMessageResponse(Message message, String recipientName) {
//        return MessageResponseDTO.builder()
//                .id(message.getId())
//                .conversationId(message.getConversation().getId())
//                .senderName(message.getSender().getUsername())
//                .recipientName(recipientName)
//                .content(message.getContent())
//                .status(message.getStatus())
//                .timeStamp(message.getCreatedAt())
//                .build();
//    }
//}