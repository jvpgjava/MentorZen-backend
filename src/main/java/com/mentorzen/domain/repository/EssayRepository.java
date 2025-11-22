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
import java.util.Optional;

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

    @Query("SELECT e FROM Essay e WHERE e.title LIKE %:keyword% OR e.theme LIKE %:keyword% OR e.content LIKE %:keyword%")
    Page<Essay> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT e FROM Essay e WHERE e.user.id = :userId AND (e.title LIKE %:keyword% OR e.theme LIKE %:keyword% OR e.content LIKE %:keyword%)")
    Page<Essay> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
}

