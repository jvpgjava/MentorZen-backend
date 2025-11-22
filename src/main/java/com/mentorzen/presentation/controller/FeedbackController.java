package com.mentorzen.presentation.controller;

import com.mentorzen.application.dto.response.FeedbackResponse;
import com.mentorzen.application.service.FeedbackService;
import com.mentorzen.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mentorzen.domain.repository.UserRepository;
import com.mentorzen.infrastructure.exception.BusinessException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
@Tag(name = "Feedbacks", description = "Endpoints para visualização e gerenciamento de feedbacks das redações")
@SecurityRequirement(name = "Bearer Authentication")
public class FeedbackController {

        private final FeedbackService feedbackService;
        private final UserRepository userRepository;

        private User getCurrentUser(Authentication authentication) {
                String email = authentication.getName();
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        }

        @GetMapping("/essay/{essayId}")
        @Operation(summary = "Buscar feedbacks de uma redação", description = "Retorna todos os feedbacks (IA, humano, peer review) de uma redação específica")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de feedbacks retornada com sucesso", content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Redação não encontrada"),
                        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
        })
        public ResponseEntity<List<FeedbackResponse>> getEssayFeedbacks(
                        @PathVariable @Parameter(description = "ID da redação", required = true, example = "1") Long essayId,
                        Authentication authentication) {

                User user = getCurrentUser(authentication);
                log.info("Buscando feedbacks da redação ID: {} para usuário: {}", essayId, user.getEmail());
                List<FeedbackResponse> response = feedbackService.getEssayFeedbacks(essayId, user);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Buscar feedback por ID", description = "Retorna um feedback específico com análise detalhada por competências")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feedback encontrado com sucesso"),
                        @ApiResponse(responseCode = "404", description = "Feedback não encontrado"),
                        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
        })
        public ResponseEntity<FeedbackResponse> getFeedback(
                        @PathVariable @Parameter(description = "ID do feedback", required = true, example = "1") Long id,
                        Authentication authentication) {

                User user = getCurrentUser(authentication);
                log.info("Buscando feedback ID: {} para usuário: {}", id, user.getEmail());
                FeedbackResponse response = feedbackService.getFeedbackById(id, user);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/user")
        @Operation(summary = "Listar todos os feedbacks do usuário", description = "Retorna todos os feedbacks recebidos pelo usuário autenticado, ordenados por data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de feedbacks do usuário")
        })
        public ResponseEntity<List<FeedbackResponse>> getUserFeedbacks(
                        Authentication authentication) {

                User user = getCurrentUser(authentication);
                log.info("Buscando todos os feedbacks do usuário: {}", user.getEmail());
                List<FeedbackResponse> response = feedbackService.getUserFeedbacks(user);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/user/stats")
        @Operation(summary = "Estatísticas de desempenho do usuário", description = "Retorna estatísticas consolidadas: nota média, número de feedbacks e total de redações")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Estatísticas calculadas com sucesso", content = @Content(schema = @Schema(example = """
                                        {
                                            "averageScore": 720.5,
                                            "feedbackCount": 15,
                                            "totalEssays": 15
                                        }
                                        """)))
        })
        public ResponseEntity<Map<String, Object>> getUserStats(
                        Authentication authentication) {

                User user = getCurrentUser(authentication);
                log.info("Buscando estatísticas do usuário: {}", user.getEmail());

                Double averageScore = feedbackService.getUserAverageScore(user);
                Long feedbackCount = feedbackService.getUserFeedbackCount(user);

                Map<String, Object> stats = Map.of(
                                "averageScore", averageScore,
                                "feedbackCount", feedbackCount,
                                "totalEssays", feedbackCount);

                return ResponseEntity.ok(stats);
        }
}
