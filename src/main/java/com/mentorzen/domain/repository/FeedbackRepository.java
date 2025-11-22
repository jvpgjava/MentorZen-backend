package com.mentorzen.domain.repository;

import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByEssay(Essay essay);

    List<Feedback> findByEssayOrderByCreatedAtDesc(Essay essay);

    Optional<Feedback> findByEssayAndType(Essay essay, Feedback.FeedbackType type);

    List<Feedback> findByReviewer(User reviewer);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId ORDER BY f.createdAt DESC")
    List<Feedback> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.essay.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(f.overallScore) FROM Feedback f WHERE f.essay.user.id = :userId AND f.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Feedback f WHERE f.type = :type ORDER BY f.createdAt DESC")
    List<Feedback> findByTypeOrderByCreatedAtDesc(@Param("type") Feedback.FeedbackType type);

    @Query("SELECT f FROM Feedback f WHERE f.essay.id = :essayId AND f.type = 'AI_GENERATED'")
    Optional<Feedback> findAiFeedbackByEssayId(@Param("essayId") Long essayId);
}

