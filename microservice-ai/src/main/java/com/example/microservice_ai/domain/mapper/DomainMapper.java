package com.example.microservice_ai.domain.mapper;

import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;

public class DomainMapper {

    public static ChatAggregate toDomain(Chat entity) {
        if (entity == null) return null;
        return new ChatAggregate(entity.getId(), entity.getTitle(), entity.getCreatedAt());
    }

    public static Chat toEntity(ChatAggregate domain) {
        if (domain == null) return null;
        Chat entity = new Chat();
        entity.setTitle(domain.getTitle());
        return entity;
    }

    /**
     * Updates an existing Chat entity with values from a ChatAggregate domain object.
     * This preserves the entity's identity (id, createdAt) while updating mutable fields.
     */
    public static void updateEntity(Chat entity, ChatAggregate domain) {
        if (entity == null || domain == null) return;
        entity.setTitle(domain.getTitle());
    }

    public static MessageModel toDomain(Message entity) {
        if (entity == null) return null;
        return new MessageModel(
            entity.getId(),
            entity.getRole(),
            entity.getContent(),
            entity.getChat() != null ? entity.getChat().getId() : null,
            entity.getCreatedAt()
        );
    }

    public static Message toEntity(MessageModel domain, Chat chat) {
        if (domain == null) return null;
        Message entity = new Message(domain.getRole(), domain.getContent(), chat);
        return entity;
    }
}
