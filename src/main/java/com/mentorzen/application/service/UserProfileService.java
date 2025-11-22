package com.mentorzen.application.service;

import com.mentorzen.application.dto.request.ChangePasswordRequest;
import com.mentorzen.application.dto.request.UpdateProfileRequest;
import com.mentorzen.application.dto.response.MessageResponse;
import com.mentorzen.application.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
    UserResponse updateProfile(String userEmail, UpdateProfileRequest request);
    MessageResponse changePassword(String userEmail, ChangePasswordRequest request);
    UserResponse uploadProfilePicture(String userEmail, MultipartFile file);
    MessageResponse deleteAccount(String userEmail);
}
