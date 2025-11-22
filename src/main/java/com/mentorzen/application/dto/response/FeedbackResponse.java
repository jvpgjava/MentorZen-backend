package com.mentorzen.application.dto.response;

import com.mentorzen.domain.entity.Feedback;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {

    private Long id;
    private Integer overallScore;
    private Integer competence1Score;
    private Integer competence2Score;
    private Integer competence3Score;
    private Integer competence4Score;
    private Integer competence5Score;
    private String generalComment;
    private String competence1Comment;
    private String competence2Comment;
    private String competence3Comment;
    private String competence4Comment;
    private String competence5Comment;
    private String suggestions;
    private String positivePoints;
    private Feedback.FeedbackType type;
    private LocalDateTime createdAt;
    private Long essayId;
    private UserResponse reviewer;

    public static FeedbackResponse fromEntity(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .overallScore(feedback.getOverallScore())
                .competence1Score(feedback.getCompetence1Score())
                .competence2Score(feedback.getCompetence2Score())
                .competence3Score(feedback.getCompetence3Score())
                .competence4Score(feedback.getCompetence4Score())
                .competence5Score(feedback.getCompetence5Score())
                .generalComment(feedback.getGeneralComment())
                .competence1Comment(feedback.getCompetence1Comment())
                .competence2Comment(feedback.getCompetence2Comment())
                .competence3Comment(feedback.getCompetence3Comment())
                .competence4Comment(feedback.getCompetence4Comment())
                .competence5Comment(feedback.getCompetence5Comment())
                .suggestions(feedback.getSuggestions())
                .positivePoints(feedback.getPositivePoints())
                .type(feedback.getType())
                .createdAt(feedback.getCreatedAt())
                .essayId(feedback.getEssay() != null ? feedback.getEssay().getId() : null)
                .reviewer(feedback.getReviewer() != null ? UserResponse.fromEntity(feedback.getReviewer()) : null)
                .build();
    }
}

