package com.mentorzen.application.service.impl;

import com.mentorzen.application.dto.request.ForgotPasswordRequest;
import com.mentorzen.application.dto.request.LoginRequest;
import com.mentorzen.application.dto.request.RegisterRequest;
import com.mentorzen.application.dto.request.ResetPasswordRequest;
import com.mentorzen.application.dto.response.AuthResponse;
import com.mentorzen.application.dto.response.MessageResponse;
import com.mentorzen.application.dto.response.UserResponse;
import com.mentorzen.application.service.AuthService;
import com.mentorzen.application.service.security.JwtService;
import com.mentorzen.domain.entity.PasswordResetToken;
import com.mentorzen.domain.entity.User;
import com.mentorzen.domain.repository.PasswordResetTokenRepository;
import com.mentorzen.domain.repository.UserRepository;
import com.mentorzen.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Tentativa de login com senha incorreta para email: {}", request.getEmail());
            throw new BusinessException("Credenciais inválidas");
        }

        String token = jwtService.encode(user);
        UserResponse userResponse = mapToUserResponse(user);

        log.info("Login realizado com sucesso para usuário: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentativa de registro para email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já está em uso");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.STUDENT)
                .schoolGrade(request.getSchoolGrade())
                .studyGoals(request.getStudyGoals())
                .build();

        user = userRepository.save(user);

        String token = jwtService.encode(user);
        UserResponse userResponse = mapToUserResponse(user);

        log.info("Usuário registrado com sucesso: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Solicitação de recuperação de senha para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BusinessException("Email não encontrado"));

        passwordResetTokenRepository.invalidateAllTokensByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        passwordResetTokenRepository.save(resetToken);

        log.info("Token de recuperação gerado para usuário: {} - Token: {}", user.getEmail(), token);

        return MessageResponse.of("Email de recuperação enviado com sucesso");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        log.info("Tentativa de reset de senha com token: {}", request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Token inválido"));

        if (!resetToken.isValid()) {
            throw new BusinessException("Token expirado ou já utilizado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Senha alterada com sucesso para usuário: {}", user.getEmail());

        return MessageResponse.of("Senha alterada com sucesso");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.fromEntity(user);
    }
}
