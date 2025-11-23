package com.mentorzen.domain.repository;

import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EssayRepository extends JpaRepository<Essay, Long> {

    Page<Essay> findByUser(User user, Pageable pageable);

    Page<Essay> findByUserAndStatus(User user, Essay.EssayStatus status, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId ORDER BY e.updatedAt DESC")
    List<Essay> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND e.status = :status ORDER BY e.updatedAt DESC")
    List<Essay> findByUserIdAndStatusOrderByUpdatedAtDesc(@Param("userId") Long userId, @Param("status") Essay.EssayStatus status);

    @Query("SELECT COUNT(e) FROM Essay e WHERE e.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM Essay e WHERE e.user.id = :userId AND e.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Essay.EssayStatus status);

    @Query("SELECT e FROM Essay e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    List<Essay> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Essay e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.theme) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Essay> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.theme) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Essay> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND e.status = :status AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.theme) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Essay> findByUserIdAndStatusAndKeyword(@Param("userId") Long userId, @Param("status") Essay.EssayStatus status, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND e.status = :status")
    Page<Essay> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Essay.EssayStatus status, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    Page<Essay> findByUserIdAndDate(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND e.status = :status AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    Page<Essay> findByUserIdAndStatusAndDate(@Param("userId") Long userId, @Param("status") Essay.EssayStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.theme) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    Page<Essay> findByUserIdAndKeywordAndDate(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND e.status = :status AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.theme) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    Page<Essay> findByUserIdAndStatusAndKeywordAndDate(@Param("userId") Long userId, @Param("status") Essay.EssayStatus status, @Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}

