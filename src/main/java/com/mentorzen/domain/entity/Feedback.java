package com.mentorzen.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "overall_score")
    @Min(value = 0, message = "Nota deve ser no mínimo 0")
    @Max(value = 1000, message = "Nota deve ser no máximo 1000")
    private Integer overallScore;

    @Column(name = "competence_1_score")
    @Min(value = 0, message = "Nota da competência 1 deve ser no mínimo 0")
    @Max(value = 200, message = "Nota da competência 1 deve ser no máximo 200")
    private Integer competence1Score;

    @Column(name = "competence_2_score")
    @Min(value = 0, message = "Nota da competência 2 deve ser no mínimo 0")
    @Max(value = 200, message = "Nota da competência 2 deve ser no máximo 200")
    private Integer competence2Score;

    @Column(name = "competence_3_score")
    @Min(value = 0, message = "Nota da competência 3 deve ser no mínimo 0")
    @Max(value = 200, message = "Nota da competência 3 deve ser no máximo 200")
    private Integer competence3Score;

    @Column(name = "competence_4_score")
    @Min(value = 0, message = "Nota da competência 4 deve ser no mínimo 0")
    @Max(value = 200, message = "Nota da competência 4 deve ser no máximo 200")
    private Integer competence4Score;

    @Column(name = "competence_5_score")
    @Min(value = 0, message = "Nota da competência 5 deve ser no mínimo 0")
    @Max(value = 200, message = "Nota da competência 5 deve ser no máximo 200")
    private Integer competence5Score;

    @NotBlank(message = "Comentário geral é obrigatório")
    @Column(name = "general_comment", nullable = false, columnDefinition = "TEXT")
    private String generalComment;

    @Column(name = "competence_1_comment", columnDefinition = "TEXT")
    private String competence1Comment;

    @Column(name = "competence_2_comment", columnDefinition = "TEXT")
    private String competence2Comment;

    @Column(name = "competence_3_comment", columnDefinition = "TEXT")
    private String competence3Comment;

    @Column(name = "competence_4_comment", columnDefinition = "TEXT")
    private String competence4Comment;

    @Column(name = "competence_5_comment", columnDefinition = "TEXT")
    private String competence5Comment;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "positive_points", columnDefinition = "TEXT")
    private String positivePoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType type = FeedbackType.AI_GENERATED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "essay_id", nullable = false)
    private Essay essay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (overallScore == null && hasAllCompetenceScores()) {
            overallScore = calculateOverallScore();
        }
    }

    private boolean hasAllCompetenceScores() {
        return competence1Score != null && competence2Score != null &&
                competence3Score != null && competence4Score != null &&
                competence5Score != null;
    }

    private Integer calculateOverallScore() {
        return competence1Score + competence2Score + competence3Score +
                competence4Score + competence5Score;
    }

    public enum FeedbackType {
        AI_GENERATED, HUMAN_REVIEW, PEER_REVIEW
    }
}

