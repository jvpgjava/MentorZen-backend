package com.mentorzen.application.service;

import com.mentorzen.application.dto.response.FeedbackResponse;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.domain.entity.User;

import java.util.List;

public interface FeedbackService {

    Feedback saveFeedback(Feedback feedback);

    List<FeedbackResponse> getEssayFeedbacks(Long essayId, User user);

    FeedbackResponse getFeedbackById(Long id, User user);

    List<FeedbackResponse> getUserFeedbacks(User user);

    Double getUserAverageScore(User user);

    Long getUserFeedbackCount(User user);
}

