package org.example.microserviceaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Email or username are mandatory")
    private String loginIdentifier; // can be username or email
    @NotBlank(message = "Password is mandatory")
    private String password;
}
