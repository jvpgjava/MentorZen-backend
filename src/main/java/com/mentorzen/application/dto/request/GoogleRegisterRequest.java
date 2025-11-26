package com.mentorzen.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GoogleRegisterRequest {
    @NotBlank(message = "Token do Google é obrigatório")
    private String token;

    @Size(max = 50, message = "Série/Ano deve ter no máximo 50 caracteres")
    private String schoolGrade;

    @Size(max = 500, message = "Objetivos de estudo devem ter no máximo 500 caracteres")
    private String studyGoals;
}
