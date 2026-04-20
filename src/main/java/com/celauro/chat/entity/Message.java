package com.celauro.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(length = 200)
    private String text;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User receiver;
    
    private long timestamp;
    private boolean isRead;

    

    public Message() {}

    public Message(User sender, User receiver, String text, boolean isRead) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.isRead = isRead;
        this.timestamp = System.currentTimeMillis();
    }    
}
