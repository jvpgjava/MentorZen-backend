package com.mentorzen.application.dto.response;

import com.mentorzen.domain.entity.Essay;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EssayResponse {

    private Long id;
    private String title;
    private String theme;
    private String content;
    private Essay.EssayStatus status;
    private Integer wordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private UserResponse user;
    private List<FeedbackResponse> feedbacks;

    public static EssayResponse fromEntity(Essay essay) {
        return EssayResponse.builder()
                .id(essay.getId())
                .title(essay.getTitle())
                .theme(essay.getTheme())
                .content(essay.getContent())
                .status(essay.getStatus())
                .wordCount(essay.getWordCount())
                .createdAt(essay.getCreatedAt())
                .updatedAt(essay.getUpdatedAt())
                .submittedAt(essay.getSubmittedAt())
                .user(essay.getUser() != null ? UserResponse.fromEntity(essay.getUser()) : null)
                .build();
    }

    public static EssayResponse fromEntityWithFeedbacks(Essay essay) {
        EssayResponse response = fromEntity(essay);
        if (essay.getFeedbacks() != null) {
            response.setFeedbacks(essay.getFeedbacks().stream()
                    .map(FeedbackResponse::fromEntity)
                    .toList());
        }
        return response;
    }
}

