package com.example.microservice_ai.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.microservice_ai.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByChatId(Long chatId);
}