package com.mentorzen.application.service.impl;

import com.mentorzen.application.dto.request.ChangePasswordRequest;
import com.mentorzen.application.dto.request.UpdateProfileRequest;
import com.mentorzen.application.dto.response.MessageResponse;
import com.mentorzen.application.dto.response.UserResponse;
import com.mentorzen.application.service.UserProfileService;
import com.mentorzen.domain.entity.User;
import com.mentorzen.domain.repository.UserRepository;
import com.mentorzen.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.profile-pictures.path:uploads/profile-pictures}")
    private String uploadPath;

    @Value("${app.upload.profile-pictures.max-size:26214400}") // 25MB
    private long maxFileSize;

    @Override
    @Transactional
    public UserResponse updateProfile(String userEmail, UpdateProfileRequest request) {
        log.info("Atualizando perfil do usuário: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email já está em uso por outro usuário");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }

        if (request.getSchoolGrade() != null) {
            user.setSchoolGrade(request.getSchoolGrade().trim().isEmpty() ? null : request.getSchoolGrade().trim());
        }

        if (request.getStudyGoals() != null) {
            user.setStudyGoals(request.getStudyGoals().trim().isEmpty() ? null : request.getStudyGoals().trim());
        }

        user = userRepository.save(user);

        log.info("Perfil atualizado com sucesso para usuário: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public MessageResponse changePassword(String userEmail, ChangePasswordRequest request) {
        log.info("Alterando senha do usuário: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Senha alterada com sucesso para usuário: {}", user.getEmail());
        return MessageResponse.of("Senha alterada com sucesso");
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePicture(String userEmail, MultipartFile file) {
        log.info("Fazendo upload de foto de perfil para usuário: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (file.isEmpty()) {
            throw new BusinessException("Arquivo não pode estar vazio");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("Arquivo muito grande. Tamanho máximo: 25MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Arquivo deve ser uma imagem");
        }

        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String profilePictureUrl = "/uploads/profile-pictures/" + filename;
            user.setProfilePictureUrl(profilePictureUrl);
            user = userRepository.save(user);

            log.info("Foto de perfil salva com sucesso: {}", profilePictureUrl);
            return UserResponse.fromEntity(user);

        } catch (IOException e) {
            log.error("Erro ao salvar arquivo de foto de perfil", e);
            throw new BusinessException("Erro ao salvar foto de perfil");
        }
    }

    @Override
    @Transactional
    public MessageResponse deleteAccount(String userEmail) {
        log.info("Deletando conta do usuário: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (user.getProfilePictureUrl() != null) {
            try {
                Path filePath = Paths.get(uploadPath,
                        user.getProfilePictureUrl().substring(user.getProfilePictureUrl().lastIndexOf("/") + 1));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Erro ao deletar foto de perfil: {}", e.getMessage());
            }
        }

        userRepository.delete(user);

        log.info("Conta deletada com sucesso: {}", userEmail);
        return MessageResponse.of("Conta deletada com sucesso");
    }
}
