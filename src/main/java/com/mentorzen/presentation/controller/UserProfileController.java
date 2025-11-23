package com.mentorzen.presentation.controller;

import com.mentorzen.application.dto.request.ChangePasswordRequest;
import com.mentorzen.application.dto.request.UpdateProfileRequest;
import com.mentorzen.application.dto.response.MessageResponse;
import com.mentorzen.application.dto.response.UserResponse;
import com.mentorzen.application.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Profile", description = "Endpoints para gerenciamento de perfil do usuário")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping
    @Operation(summary = "Atualizar perfil", description = "Atualiza as informações do perfil do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email já está em uso"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userProfileService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = userProfileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/picture")
    @Operation(summary = "Upload foto de perfil", description = "Faz upload da foto de perfil do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto enviada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou muito grande"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<UserResponse> uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        UserResponse response = userProfileService.uploadProfilePicture(authentication.getName(), file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Deletar conta", description = "Deleta permanentemente a conta do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta deletada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<MessageResponse> deleteAccount(Authentication authentication) {
        MessageResponse response = userProfileService.deleteAccount(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
