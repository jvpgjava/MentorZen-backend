package com.mentorzen.application.service.impl;

import com.google.auth.oauth2.TokenVerifier;
import com.mentorzen.application.dto.request.ForgotPasswordRequest;
import com.mentorzen.application.dto.request.GoogleLoginRequest;
import com.mentorzen.application.dto.request.GoogleRegisterRequest;
import com.mentorzen.application.dto.request.LoginRequest;
import com.mentorzen.application.dto.request.RegisterRequest;
import com.mentorzen.application.dto.request.ResetPasswordRequest;
import com.mentorzen.application.dto.response.AuthResponse;
import com.mentorzen.application.dto.response.MessageResponse;
import com.mentorzen.application.dto.response.UserResponse;
import com.mentorzen.application.service.AuthService;
import com.mentorzen.application.service.EmailService;
import com.mentorzen.application.service.security.JwtService;
import com.mentorzen.domain.entity.PasswordResetToken;
import com.mentorzen.domain.entity.User;
import com.mentorzen.domain.repository.PasswordResetTokenRepository;
import com.mentorzen.domain.repository.UserRepository;
import com.mentorzen.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailService emailService;

    @Value("${app.google.client-id:}")
    private String googleClientId;

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
    @Transactional(readOnly = true)
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("Tentativa de login com Google");

        try {
            if (googleClientId == null || googleClientId.isEmpty()) {
                throw new BusinessException("Google Client ID não configurado");
            }

            TokenVerifier verifier = TokenVerifier.newBuilder()
                    .setAudience(googleClientId)
                    .setIssuer("https://accounts.google.com")
                    .build();

            var token = verifier.verify(request.getToken());
            var payload = token.getPayload();
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            if (email == null || email.isEmpty()) {
                throw new BusinessException("Email não encontrado no token do Google");
            }

            User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Usuário não encontrado. Por favor, crie uma conta primeiro."));

            log.info("Login com Google para usuário existente: {}", email);

            if ((user.getName() == null || user.getName().isEmpty()) && name != null && !name.isEmpty()) {
                user.setName(name);
            }

            boolean needsUpdate = false;
            if (picture != null && !picture.isEmpty()) {
                if (user.getProfilePictureUrl() == null || !picture.equals(user.getProfilePictureUrl())) {
                    user.setProfilePictureUrl(picture);
                    needsUpdate = true;
                }
            }

            if (needsUpdate || (user.getName() == null || user.getName().isEmpty())) {
                userRepository.save(user);
            }

            String jwtToken = jwtService.encode(user);
            UserResponse userResponse = mapToUserResponse(user);

            log.info("Login realizado com sucesso via Google para: {}", email);

            return AuthResponse.builder().token(jwtToken).user(userResponse).build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao fazer login com Google", e);
            throw new BusinessException("Token do Google inválido ou expirado");
        }
    }

    @Override
    @Transactional
    public AuthResponse registerWithGoogle(GoogleRegisterRequest request) {
        log.info("Tentativa de registro com Google");

        try {
            if (googleClientId == null || googleClientId.isEmpty()) {
                throw new BusinessException("Google Client ID não configurado");
            }

            TokenVerifier verifier = TokenVerifier.newBuilder()
                    .setAudience(googleClientId)
                    .setIssuer("https://accounts.google.com")
                    .build();

            var token = verifier.verify(request.getToken());
            var payload = token.getPayload();
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            if (email == null || email.isEmpty()) {
                throw new BusinessException("Email não encontrado no token do Google");
            }

            if (userRepository.existsByEmail(email)) {
                throw new BusinessException("Email já está cadastrado. Faça login em vez de criar uma nova conta.");
            }

            log.info("Registrando novo usuário via Google: {}", email);
            User user = User.builder().name(name != null && !name.isEmpty() ? name : email.split("@")[0])
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(User.UserRole.STUDENT)
                    .schoolGrade(request.getSchoolGrade())
                    .studyGoals(request.getStudyGoals())
                    .profilePictureUrl(picture)
                    .build();
            user = userRepository.save(user);
            log.info("Usuário registrado com sucesso via Google: {}", email);

            String jwtToken = jwtService.encode(user);
            UserResponse userResponse = mapToUserResponse(user);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .user(userResponse)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao fazer registro com Google", e);
            throw new BusinessException("Token do Google inválido ou expirado");
        }
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

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            log.warn("Tentativa de recuperação de senha para email não cadastrado: {}", request.getEmail());
            return MessageResponse.of("Se o email estiver cadastrado, você receberá um link de recuperação");
        }

        passwordResetTokenRepository.invalidateAllTokensByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        log.info("Token de recuperação gerado para usuário: {} - Token: {}", user.getEmail(), token);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            log.info("Email de recuperação enviado com sucesso para: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erro ao enviar email de recuperação de senha para: {}", user.getEmail(), e);
        }

        return MessageResponse.of("Se o email estiver cadastrado, você receberá um link de recuperação");
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
