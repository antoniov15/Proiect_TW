package org.example.microserviceaccount.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter
@Getter
public class AccountResponseDTO {
    private Long id;
    private String userName;
    private String email;
    private LocalDate createdAt;
}