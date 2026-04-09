package com.beta.FindHome.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_participant_user_conversation",
                        columnNames = {"user_id", "conversation_id"}
                )
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Participant extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participant_user"))
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participant_conversation"))
    private Conversation conversation;
}

//package com.beta.FindHome.model;
//
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.Entity;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//import lombok.*;
//
//import java.io.Serial;
//import java.io.Serializable;
//
//@Entity
//@Table(name = "participants")
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Getter
//@Setter
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Participant.class
//)
//public class Participant extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private Users user;
//
//    @ManyToOne
//    @JoinColumn(name = "conversation_id", nullable = false)
//    private Conversation conversation;
//}
