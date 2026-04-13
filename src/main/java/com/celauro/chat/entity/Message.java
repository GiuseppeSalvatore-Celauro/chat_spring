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
    private User user;

    private long timestamp;

    

    public Message() {}

    public Message(User user, String text) {
        this.user = user;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }    
}
