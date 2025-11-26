package com.mentorzen.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "Token do Google é obrigatório")
    private String token;
}
