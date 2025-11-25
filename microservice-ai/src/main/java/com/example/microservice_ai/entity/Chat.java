package com.example.microservice_ai.entity;

import java.time.Instant;

import com.example.microservice_ai.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat")
public class Chat {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="chat_id")
  private Long id;

  @Column(name="title")
  private String title;

  @Column(name="created_at")
  private Instant createdAt = Instant.now();

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public Instant getCreatedAt() { return createdAt; }
}
