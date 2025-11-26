package com.mentorzen.domain.repository;

import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId ORDER BY f.createdAt DESC")
    Page<Feedback> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.essay.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(f.overallScore) FROM Feedback f WHERE f.essay.user.id = :userId AND f.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Feedback f WHERE f.type = :type ORDER BY f.createdAt DESC")
    List<Feedback> findByTypeOrderByCreatedAtDesc(@Param("type") Feedback.FeedbackType type);

    @Query("SELECT f FROM Feedback f WHERE f.essay.id = :essayId AND f.type = 'AI_GENERATED'")
    Optional<Feedback> findAiFeedbackByEssayId(@Param("essayId") Long essayId);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND f.type = :type")
    Page<Feedback> findByUserIdAndType(@Param("userId") Long userId, @Param("type") Feedback.FeedbackType type,
            Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND (LOWER(f.essay.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.generalComment) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Feedback> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND f.createdAt >= :startDate AND f.createdAt < :endDate")
    Page<Feedback> findByUserIdAndDate(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND f.type = :type AND (LOWER(f.essay.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.generalComment) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Feedback> findByUserIdAndTypeAndKeyword(@Param("userId") Long userId,
            @Param("type") Feedback.FeedbackType type, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND f.type = :type AND f.createdAt >= :startDate AND f.createdAt < :endDate")
    Page<Feedback> findByUserIdAndTypeAndDate(@Param("userId") Long userId, @Param("type") Feedback.FeedbackType type,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND (LOWER(f.essay.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.generalComment) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND f.createdAt >= :startDate AND f.createdAt < :endDate")
    Page<Feedback> findByUserIdAndKeywordAndDate(@Param("userId") Long userId, @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.essay.user.id = :userId AND f.type = :type AND (LOWER(f.essay.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.generalComment) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND f.createdAt >= :startDate AND f.createdAt < :endDate")
    Page<Feedback> findByUserIdAndTypeAndKeywordAndDate(@Param("userId") Long userId,
            @Param("type") Feedback.FeedbackType type, @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}
