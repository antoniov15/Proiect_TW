package com.example.microservice_ai.entity;

import jakarta.persistence.*;
import com.example.microservice_ai.enums.Role;

@Entity
@Table(name = "message")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="message_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name="content")
    private String content;

     @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;


    @Column(name = "created_at")
    private java.time.Instant createdAt = java.time.Instant.now();

    public Message() {}
    public Message(Role role, String content, Chat chat) {
        this.role = role;
        this.content = content;
        this.chat = chat;
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public Role getRole() { return this.role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat;}

    public java.time.Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
}
