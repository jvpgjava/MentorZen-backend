package com.mentorzen.application.service.impl;

import com.mentorzen.application.dto.response.FeedbackResponse;
import com.mentorzen.application.service.FeedbackService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.domain.entity.User;
import com.mentorzen.domain.repository.EssayRepository;
import com.mentorzen.domain.repository.FeedbackRepository;
import com.mentorzen.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EssayRepository essayRepository;

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        log.info("Salvando feedback para redação ID: {}", feedback.getEssay().getId());
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getEssayFeedbacks(Long essayId, User user) {
        log.info("Buscando feedbacks da redação ID: {} para usuário ID: {}", essayId, user.getId());

        Essay essay = essayRepository.findById(essayId)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));

        List<Feedback> feedbacks = feedbackRepository.findByEssayOrderByCreatedAtDesc(essay);
        return feedbacks.stream()
                .map(FeedbackResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Long id, User user) {
        log.info("Buscando feedback ID: {} para usuário ID: {}", id, user.getId());

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback não encontrado"));

        if (!feedback.getEssay().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Feedback não encontrado");
        }

        return FeedbackResponse.fromEntity(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getUserFeedbacks(User user) {
        log.info("Buscando todos os feedbacks do usuário ID: {}", user.getId());

        List<Feedback> feedbacks = feedbackRepository.findByUserId(user.getId());
        return feedbacks.stream()
                .map(FeedbackResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getUserAverageScore(User user) {
        log.info("Calculando média de notas do usuário ID: {}", user.getId());

        Double average = feedbackRepository.getAverageScoreByUserId(user.getId());
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserFeedbackCount(User user) {
        log.info("Contando feedbacks do usuário ID: {}", user.getId());

        return feedbackRepository.countByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getUserFeedbacksWithFilters(User user, Feedback.FeedbackType type, String keyword,
            java.time.LocalDate date, Pageable pageable) {
        log.info("Buscando feedbacks do usuário ID: {} com filtros - type: {}, keyword: {}, date: {}",
                user.getId(), type, keyword, date);

        Page<Feedback> feedbacks;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String searchKeyword = hasKeyword ? keyword.trim() : null;
        boolean hasDate = date != null;

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (hasDate) {
            startDate = date.atStartOfDay();
            endDate = date.plusDays(1).atStartOfDay();
        }

        if (type != null && hasKeyword && hasDate) {
            feedbacks = feedbackRepository.findByUserIdAndTypeAndKeywordAndDate(user.getId(), type, searchKeyword,
                    startDate, endDate, pageable);
        } else if (type != null && hasKeyword) {
            feedbacks = feedbackRepository.findByUserIdAndTypeAndKeyword(user.getId(), type, searchKeyword, pageable);
        } else if (type != null && hasDate) {
            feedbacks = feedbackRepository.findByUserIdAndTypeAndDate(user.getId(), type, startDate, endDate, pageable);
        } else if (hasKeyword && hasDate) {
            feedbacks = feedbackRepository.findByUserIdAndKeywordAndDate(user.getId(), searchKeyword, startDate,
                    endDate, pageable);
        } else if (type != null) {
            feedbacks = feedbackRepository.findByUserIdAndType(user.getId(), type, pageable);
        } else if (hasKeyword) {
            feedbacks = feedbackRepository.findByUserIdAndKeyword(user.getId(), searchKeyword, pageable);
        } else if (hasDate) {
            feedbacks = feedbackRepository.findByUserIdAndDate(user.getId(), startDate, endDate, pageable);
        } else {
            feedbacks = feedbackRepository.findByUserId(user.getId(), pageable);
        }

        return feedbacks.map(FeedbackResponse::fromEntity);
    }
}
