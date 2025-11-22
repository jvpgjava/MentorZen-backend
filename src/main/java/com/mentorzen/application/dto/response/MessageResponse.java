package com.mentorzen.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
