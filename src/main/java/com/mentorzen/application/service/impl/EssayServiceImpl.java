package com.mentorzen.application.service.impl;

import com.mentorzen.application.dto.request.EssayCreateRequest;
import com.mentorzen.application.dto.response.EssayResponse;
import com.mentorzen.application.service.AsyncEssayAnalysisService;
import com.mentorzen.application.service.EssayAnalysisService;
import com.mentorzen.application.service.EssayService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.User;
import com.mentorzen.domain.repository.EssayRepository;
import com.mentorzen.infrastructure.exception.BusinessException;
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
public class EssayServiceImpl implements EssayService {

    private final EssayRepository essayRepository;
    private final EssayAnalysisService analysisService;
    private final AsyncEssayAnalysisService asyncEssayAnalysisService;

    @Override
    public EssayResponse createEssay(EssayCreateRequest request, User user) {
        log.info("Criando nova redação para usuário ID: {}", user.getId());

        Essay.EssayType essayType = parseEssayType(request.getEssayType());

        Essay essay = Essay.builder()
                .title(request.getTitle())
                .theme(request.getTheme())
                .essayType(essayType)
                .content(request.getContent())
                .status(Essay.EssayStatus.DRAFT)
                .user(user)
                .build();

        Essay savedEssay = essayRepository.save(essay);
        log.info("Redação criada com ID: {}", savedEssay.getId());

        return EssayResponse.fromEntity(savedEssay);
    }

    @Override
    public EssayResponse updateEssay(Long id, EssayCreateRequest request, User user) {
        log.info("Atualizando redação ID: {} para usuário ID: {}", id, user.getId());

        Essay essay = findEssayByIdAndUser(id, user);

        if (essay.getStatus() == Essay.EssayStatus.SUBMITTED ||
                essay.getStatus() == Essay.EssayStatus.ANALYZED) {
            throw new BusinessException("Não é possível editar uma redação já submetida para análise");
        }

        essay.setTitle(request.getTitle());
        essay.setTheme(request.getTheme());
        essay.setContent(request.getContent());
        if (request.getEssayType() != null) {
            essay.setEssayType(parseEssayType(request.getEssayType()));
        }

        Essay updatedEssay = essayRepository.save(essay);
        log.info("Redação ID: {} atualizada com sucesso", id);

        return EssayResponse.fromEntity(updatedEssay);
    }

    @Override
    @Transactional(readOnly = true)
    public EssayResponse getEssayById(Long id, User user) {
        Essay essay = findEssayByIdAndUser(id, user);
        return EssayResponse.fromEntityWithFeedbacks(essay);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EssayResponse> getUserEssays(User user, Pageable pageable) {
        log.info("Buscando redações do usuário ID: {}", user.getId());

        Page<Essay> essays = essayRepository.findByUser(user, pageable);
        return essays.map(EssayResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EssayResponse> getUserEssaysByStatus(User user, Essay.EssayStatus status) {
        log.info("Buscando redações do usuário ID: {} com status: {}", user.getId(), status);

        List<Essay> essays = essayRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), status);
        return essays.stream()
                .map(EssayResponse::fromEntity)
                .toList();
    }

    @Override
    public EssayResponse submitEssayForAnalysis(Long id, User user) {
        log.info("Submetendo redação ID: {} para análise", id);

        Essay essay = findEssayByIdAndUser(id, user);

        if (essay.getStatus() != Essay.EssayStatus.DRAFT) {
            throw new BusinessException("Apenas redações em rascunho podem ser submetidas para análise");
        }

        if (!analysisService.validateEnemCriteria(essay)) {
            throw new BusinessException("A redação não atende aos critérios básicos do ENEM");
        }

        essay.setStatus(Essay.EssayStatus.SUBMITTED);
        essay.setSubmittedAt(LocalDateTime.now());
        Essay submittedEssay = essayRepository.save(essay);

        log.info("Redação ID: {} salva com status SUBMITTED. Iniciando processamento assíncrono...", submittedEssay.getId());

        try {
            asyncEssayAnalysisService.processAnalysisAsync(submittedEssay.getId());
            log.info("Método assíncrono chamado com sucesso para redação ID: {}", submittedEssay.getId());
        } catch (Exception e) {
            log.error("Erro ao chamar método assíncrono para redação ID: {}", submittedEssay.getId(), e);
        }

        return EssayResponse.fromEntity(submittedEssay);
    }

    @Override
    public EssayResponse resendEssayForAnalysis(Long id, User user) {
        log.info("Reenviando redação ID: {} para análise", id);

        Essay essay = findEssayByIdAndUser(id, user);

        if (essay.getStatus() == Essay.EssayStatus.DRAFT) {
            throw new BusinessException("Redações em rascunho devem usar o endpoint de submissão inicial");
        }

        if (essay.getStatus() == Essay.EssayStatus.ARCHIVED) {
            throw new BusinessException("Redações arquivadas não podem ser reenviadas para análise");
        }

        if (!analysisService.validateEnemCriteria(essay)) {
            throw new BusinessException("A redação não atende aos critérios básicos do ENEM");
        }

        essay.setStatus(Essay.EssayStatus.SUBMITTED);
        essay.setSubmittedAt(LocalDateTime.now());
        Essay resubmittedEssay = essayRepository.save(essay);

        log.info("Redação ID: {} salva com status SUBMITTED para reprocessamento. Iniciando processamento assíncrono...", resubmittedEssay.getId());

        try {
            asyncEssayAnalysisService.processAnalysisAsync(resubmittedEssay.getId());
            log.info("Método assíncrono chamado com sucesso para reprocessamento da redação ID: {}", resubmittedEssay.getId());
        } catch (Exception e) {
            log.error("Erro ao chamar método assíncrono para reprocessamento da redação ID: {}", resubmittedEssay.getId(), e);
        }

        return EssayResponse.fromEntity(resubmittedEssay);
    }

    @Override
    public void deleteEssay(Long id, User user) {
        log.info("Deletando redação ID: {} do usuário ID: {}", id, user.getId());

        Essay essay = findEssayByIdAndUser(id, user);

        if (essay.getStatus() == Essay.EssayStatus.ANALYZED) {
            throw new BusinessException("Não é possível deletar uma redação já analisada");
        }

        essayRepository.delete(essay);
        log.info("Redação ID: {} deletada com sucesso", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EssayResponse> searchEssays(User user, String keyword, Pageable pageable) {
        log.info("Buscando redações do usuário ID: {} com palavra-chave: {}", user.getId(), keyword);

        Page<Essay> essays = essayRepository.findByUserIdAndKeyword(user.getId(), keyword, pageable);
        return essays.map(EssayResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EssayResponse> getUserEssaysWithFilters(User user, Essay.EssayStatus status, String keyword,
            java.time.LocalDate date, Pageable pageable) {
        log.info("Buscando redações do usuário ID: {} com filtros - status: {}, keyword: {}, date: {}", user.getId(),
                status, keyword, date);

        Page<Essay> essays;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String searchKeyword = hasKeyword ? keyword.trim() : null;
        boolean hasDate = date != null;

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (hasDate) {
            startDate = date.atStartOfDay();
            endDate = date.plusDays(1).atStartOfDay();
        }

        if (status != null && hasKeyword && hasDate) {
            essays = essayRepository.findByUserIdAndStatusAndKeywordAndDate(user.getId(), status, searchKeyword,
                    startDate, endDate, pageable);
        } else if (status != null && hasKeyword) {
            essays = essayRepository.findByUserIdAndStatusAndKeyword(user.getId(), status, searchKeyword, pageable);
        } else if (status != null && hasDate) {
            essays = essayRepository.findByUserIdAndStatusAndDate(user.getId(), status, startDate, endDate, pageable);
        } else if (hasKeyword && hasDate) {
            essays = essayRepository.findByUserIdAndKeywordAndDate(user.getId(), searchKeyword, startDate, endDate,
                    pageable);
        } else if (status != null) {
            essays = essayRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        } else if (hasKeyword) {
            essays = essayRepository.findByUserIdAndKeyword(user.getId(), searchKeyword, pageable);
        } else if (hasDate) {
            essays = essayRepository.findByUserIdAndDate(user.getId(), startDate, endDate, pageable);
        } else {
            essays = essayRepository.findByUser(user, pageable);
        }

        return essays.map(EssayResponse::fromEntity);
    }

    private Essay findEssayByIdAndUser(Long id, User user) {
        return essayRepository.findById(id)
                .filter(essay -> essay.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));
    }

    private Essay.EssayType parseEssayType(String essayType) {
        if (essayType == null || essayType.isEmpty()) {
            return Essay.EssayType.ARGUMENTATIVE;
        }
        try {
            return Essay.EssayType.valueOf(essayType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Essay.EssayType.ARGUMENTATIVE;
        }
    }
}
