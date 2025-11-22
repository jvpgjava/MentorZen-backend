package com.mentorzen.application.service;

import com.mentorzen.application.dto.request.ForgotPasswordRequest;
import com.mentorzen.application.dto.request.LoginRequest;
import com.mentorzen.application.dto.request.RegisterRequest;
import com.mentorzen.application.dto.request.ResetPasswordRequest;
import com.mentorzen.application.dto.response.AuthResponse;
import com.mentorzen.application.dto.response.MessageResponse;
import com.mentorzen.application.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
    UserResponse getCurrentUser(String email);
}
