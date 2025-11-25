package org.example.microserviceaccount.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountResponseDTO {
    private Long id;
    private String userName;
    private String email;
    private LocalDate createdAt;
}