package com.mentorzen.application.service.impl;

import com.mentorzen.application.dto.response.FeedbackResponse;
import com.mentorzen.application.service.FeedbackService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.domain.entity.User;
import com.mentorzen.domain.repository.EssayRepository;
import com.mentorzen.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

