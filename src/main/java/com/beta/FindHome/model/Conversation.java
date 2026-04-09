package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.ConversationType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Conversation extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false)
    private ConversationType type;

    @Column(name = "group_name", length = 255)
    private String groupName;

    // orphanRemoval — if conversation deleted, participants deleted too
    @Builder.Default
    @OneToMany(mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<Participant> participants = new HashSet<>();

    // orphanRemoval — if conversation deleted, messages deleted too
    @Builder.Default
    @OneToMany(mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<Message> messages = new HashSet<>();
}

//package com.beta.FindHome.model;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.util.Set;
//
//import com.beta.FindHome.enums.model.ConversationType;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.*;
//
//
//@Entity
//@Table(name = "conversations")
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Setter
//@Getter
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Conversation.class
//)
//public class Conversation extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "conversion_type", nullable = false)
//    private ConversationType type;
//
//    @Column(name = "group_name",length = 255, nullable = true)
//    private String groupName;
//
//    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Participant> participants;
//
//    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Message> messages;
//}
