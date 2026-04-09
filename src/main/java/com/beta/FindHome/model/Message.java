package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.MessageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_message_sender",       columnList = "sender_id"),
        @Index(name = "idx_message_status",       columnList = "status")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Message extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_message_conversation"))
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_message_sender"))
    private Users sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status = MessageStatus.SENT;
}

//package com.beta.FindHome.model;
//
//import com.beta.FindHome.enums.model.MessageStatus;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.io.Serial;
//import java.io.Serializable;
//
//@Entity
//@Table(name = "messages")
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Getter
//@Setter
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Message.class
//)
//public class Message extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @ManyToOne
//    @JoinColumn(name = "conversation_id", nullable = false)
//    private Conversation conversation;
//
//    @ManyToOne
//    @JoinColumn(name = "sender_id", nullable = false)
//    private Users sender;
//
//    @Column(nullable = false)
//    private String content;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private MessageStatus status = MessageStatus.SENT;
//
//}
