package com.mentorzen.application.service.impl;

import com.mentorzen.application.service.AsyncEssayAnalysisService;
import com.mentorzen.application.service.EssayAnalysisService;
import com.mentorzen.application.service.FeedbackService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.domain.repository.EssayRepository;
import com.mentorzen.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEssayAnalysisServiceImpl implements AsyncEssayAnalysisService {

    private final EssayRepository essayRepository;
    private final EssayAnalysisService analysisService;
    private final FeedbackService feedbackService;

    @Override
    @Async("essayAnalysisExecutor")
    public void processAnalysisAsync(Long essayId) {
        try {
            processInTransaction(essayId);
        } catch (Exception e) {
            log.error("Erro ao analisar redação ID: {} em background", essayId, e);
            try {
                Essay essay = essayRepository.findById(essayId).orElse(null);
                if (essay != null && essay.getStatus() == Essay.EssayStatus.SUBMITTED) {
                    log.warn("Mantendo redação ID: {} com status SUBMITTED devido ao erro: {}", essayId, e.getMessage());
                }
            } catch (Exception ex) {
                log.error("Erro ao verificar status da redação ID: {}", essayId, ex);
            }
        }
    }

    @Transactional
    private Essay processInTransaction(Long essayId) {
        Essay essay = essayRepository.findById(essayId).orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));

        Feedback feedback = analysisService.analyzeEssay(essay);
        feedbackService.saveFeedback(feedback);

        essay.setStatus(Essay.EssayStatus.ANALYZED);
        Essay savedEssay = essayRepository.save(essay);

        return savedEssay;
    }
}
