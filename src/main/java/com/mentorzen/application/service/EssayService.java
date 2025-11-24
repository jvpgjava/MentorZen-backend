package com.mentorzen.application.service;

import com.mentorzen.application.dto.request.EssayCreateRequest;
import com.mentorzen.application.dto.response.EssayResponse;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EssayService {

    EssayResponse createEssay(EssayCreateRequest request, User user);

    EssayResponse updateEssay(Long id, EssayCreateRequest request, User user);

    EssayResponse getEssayById(Long id, User user);

    Page<EssayResponse> getUserEssays(User user, Pageable pageable);

    List<EssayResponse> getUserEssaysByStatus(User user, Essay.EssayStatus status);

    EssayResponse submitEssayForAnalysis(Long id, User user);

    EssayResponse resendEssayForAnalysis(Long id, User user);

    void deleteEssay(Long id, User user);

    Page<EssayResponse> searchEssays(User user, String keyword, Pageable pageable);

    Page<EssayResponse> getUserEssaysWithFilters(User user, Essay.EssayStatus status, String keyword, java.time.LocalDate date, Pageable pageable);
}

