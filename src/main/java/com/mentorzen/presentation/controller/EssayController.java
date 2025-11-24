package com.mentorzen.presentation.controller;

import com.mentorzen.application.dto.request.EssayCreateRequest;
import com.mentorzen.application.dto.response.EssayResponse;
import com.mentorzen.application.service.EssayService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mentorzen.domain.repository.UserRepository;
import com.mentorzen.infrastructure.exception.BusinessException;

import java.util.List;

@RestController
@RequestMapping("/api/essays")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Redações", description = "Endpoints para gerenciamento de redações dos estudantes")
@SecurityRequirement(name = "Bearer Authentication")
public class EssayController {

    private final EssayService essayService;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    @PostMapping
    @Operation(
            summary = "Criar nova redação",
            description = "Cria uma nova redação em rascunho para o usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Redação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = EssayResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<EssayResponse> createEssay(
            @Valid @RequestBody
            @Parameter(description = "Dados da redação a ser criada", required = true)
            EssayCreateRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Criando nova redação para usuário: {}", user.getEmail());
        EssayResponse response = essayService.createEssay(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar redação por ID",
            description = "Retorna uma redação específica do usuário autenticado, incluindo feedbacks"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Redação encontrada",
                    content = @Content(schema = @Schema(implementation = EssayResponse.class))),
            @ApiResponse(responseCode = "404", description = "Redação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<EssayResponse> getEssay(
            @PathVariable
            @Parameter(description = "ID da redação", required = true, example = "1")
            Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Buscando redação ID: {} para usuário: {}", id, user.getEmail());
        EssayResponse response = essayService.getEssayById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar redação",
            description = "Atualiza uma redação existente (apenas rascunhos podem ser editados)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Redação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Redação não pode ser editada (já foi submetida)"),
            @ApiResponse(responseCode = "404", description = "Redação não encontrada")
    })
    public ResponseEntity<EssayResponse> updateEssay(
            @PathVariable
            @Parameter(description = "ID da redação", required = true)
            Long id,
            @Valid @RequestBody EssayCreateRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Atualizando redação ID: {} para usuário: {}", id, user.getEmail());
        EssayResponse response = essayService.updateEssay(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar redação",
            description = "Remove uma redação (redações já analisadas não podem ser deletadas)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Redação deletada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Redação não pode ser deletada"),
            @ApiResponse(responseCode = "404", description = "Redação não encontrada")
    })
    public ResponseEntity<Void> deleteEssay(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Deletando redação ID: {} para usuário: {}", id, user.getEmail());
        essayService.deleteEssay(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Listar redações do usuário",
            description = "Retorna uma lista paginada das redações do usuário autenticado com filtros opcionais"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de redações retornada com sucesso")
    })
    public ResponseEntity<Page<EssayResponse>> getUserEssays(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Número da página (0-indexed)", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Tamanho da página", example = "10")
            int size,
            @RequestParam(defaultValue = "updatedAt")
            @Parameter(description = "Campo para ordenação", example = "updatedAt")
            String sortBy,
            @RequestParam(defaultValue = "desc")
            @Parameter(description = "Direção da ordenação", example = "desc")
            String sortDir,
            @RequestParam(required = false)
            @Parameter(description = "Filtrar por status", example = "DRAFT")
            Essay.EssayStatus status,
            @RequestParam(required = false)
            @Parameter(description = "Palavra-chave para busca no título, tema ou conteúdo", example = "educação")
            String keyword,
            @RequestParam(required = false)
            @Parameter(description = "Filtrar por data de criação (formato: yyyy-MM-dd)", example = "2025-11-22")
            String date,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Buscando redações do usuário: {} - página: {}, status: {}, keyword: {}, date: {}", user.getEmail(), page, status, keyword, date);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        java.time.LocalDate filterDate = null;
        if (date != null && !date.trim().isEmpty()) {
            try {
                filterDate = java.time.LocalDate.parse(date);
            } catch (Exception e) {
                log.warn("Data inválida fornecida: {}", date);
            }
        }

        Page<EssayResponse> response = essayService.getUserEssaysWithFilters(user, status, keyword, filterDate, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    @Operation(
            summary = "Submeter redação para análise",
            description = "Envia uma redação para análise automática com IA. A redação deve estar em rascunho."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Redação submetida para análise com sucesso"),
            @ApiResponse(responseCode = "400", description = "Redação não pode ser submetida (não é rascunho ou não atende critérios)"),
            @ApiResponse(responseCode = "404", description = "Redação não encontrada")
    })
    public ResponseEntity<EssayResponse> submitForAnalysis(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Submetendo redação ID: {} para análise - usuário: {}", id, user.getEmail());
        EssayResponse response = essayService.submitEssayForAnalysis(id, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resend")
    @Operation(
            summary = "Reenviar redação para análise",
            description = "Reenvia uma redação para nova análise com IA. Permite reprocessar redações que estão em análise (SUBMITTED) ou já analisadas (ANALYZED). Útil quando a análise falhou ou o estudante deseja uma nova avaliação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Redação reenviada para análise com sucesso"),
            @ApiResponse(responseCode = "400", description = "Redação não pode ser reenviada (rascunho ou arquivada)"),
            @ApiResponse(responseCode = "404", description = "Redação não encontrada")
    })
    public ResponseEntity<EssayResponse> resendForAnalysis(
            @PathVariable
            @Parameter(description = "ID da redação", required = true, example = "1")
            Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Reenviando redação ID: {} para análise - usuário: {}", id, user.getEmail());
        EssayResponse response = essayService.resendEssayForAnalysis(id, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Buscar redações por status",
            description = "Retorna todas as redações do usuário com um status específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de redações por status")
    })
    public ResponseEntity<List<EssayResponse>> getEssaysByStatus(
            @PathVariable
            @Parameter(description = "Status das redações", required = true,
                    schema = @Schema(implementation = Essay.EssayStatus.class))
            Essay.EssayStatus status,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Buscando redações com status: {} para usuário: {}", status, user.getEmail());
        List<EssayResponse> response = essayService.getUserEssaysByStatus(user, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar redações por palavra-chave",
            description = "Busca redações do usuário que contenham a palavra-chave no título, tema ou conteúdo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados da busca retornados com sucesso")
    })
    public ResponseEntity<Page<EssayResponse>> searchEssays(
            @RequestParam
            @Parameter(description = "Palavra-chave para busca", required = true, example = "educação")
            String keyword,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Número da página", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Tamanho da página", example = "10")
            int size,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        log.info("Buscando redações com palavra-chave: {} para usuário: {}", keyword, user.getEmail());

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<EssayResponse> response = essayService.searchEssays(user, keyword, pageable);
        return ResponseEntity.ok(response);
    }
}

