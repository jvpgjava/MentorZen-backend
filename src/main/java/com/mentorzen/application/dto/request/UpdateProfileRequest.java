package com.mentorzen.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Email(message = "Email deve ter formato válido")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    private String schoolGrade;

    @Size(max = 500, message = "Objetivos de estudo devem ter no máximo 500 caracteres")
    private String studyGoals;
}
