package com.mentorzen.domain.repository;

import com.mentorzen.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.id != :id")
    Optional<User> findByEmailAndNotId(@Param("email") String email, @Param("id") Long id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT'")
    Long countStudents();
}

