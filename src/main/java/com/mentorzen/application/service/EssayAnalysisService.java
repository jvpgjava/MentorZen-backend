package com.mentorzen.application.service;

import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;

public interface EssayAnalysisService {

    /**
     * Analisa uma redação usando IA e retorna feedback detalhado
     */
    Feedback analyzeEssay(Essay essay);

    /**
     * Gera sugestões de melhoria para uma redação
     */
    String generateImprovementSuggestions(Essay essay);

    /**
     * Verifica se uma redação está dentro dos critérios do ENEM
     */
    boolean validateEnemCriteria(Essay essay);
}

