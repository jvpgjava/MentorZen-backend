package com.mentorzen.application.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
}


