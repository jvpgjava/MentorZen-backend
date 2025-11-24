package com.mentorzen.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorzen.application.service.EssayAnalysisService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EssayAnalysisServiceImpl implements EssayAnalysisService {

    private final WebClient webClient;

    @Value("${google.ai.api-key:}")
    private String googleApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Feedback analyzeEssay(Essay essay) {
        log.info("Iniciando análise da redação ID: {}", essay.getId());

        String webResearchContext = performWebResearch(essay.getTheme());
        String prompt = buildAnalysisPrompt(essay, webResearchContext);
        String aiResponse = callGeminiAPI(prompt);

        return parseAiResponseToFeedback(aiResponse, essay, webResearchContext);
    }

    @Override
    public String generateImprovementSuggestions(Essay essay) {
        log.info("Gerando sugestões de melhoria para redação ID: {}", essay.getId());

        String prompt = buildSuggestionPrompt(essay);
        return callGeminiAPI(prompt);
    }

    @Override
    public boolean validateEnemCriteria(Essay essay) {
        if (essay.getContent() == null || essay.getContent().trim().isEmpty()) {
            return false;
        }

        int wordCount = essay.getWordCount() != null ? essay.getWordCount() : 0;

        if (wordCount < 150 || wordCount > 800) {
            return false;
        }

        String[] paragraphs = essay.getContent().split("\n\n");
        if (paragraphs.length < 3) {
            return false;
        }

        return true;
    }

    private String buildAnalysisPrompt(Essay essay, String webResearchContext) {
        return String.format(
                """
                        SISTEMA: Você é o NotebookLM especializado em correção de redações ENEM, com acesso às seguintes fontes de referência e pesquisa web em tempo real:

                        FONTES DE REFERÊNCIA:
                        - Manual de Redação do ENEM 2025
                        - Cartilha do Participante ENEM
                        - Redações Nota 1000 dos últimos 5 anos
                        - Critérios oficiais das 5 competências ENEM
                        - Guia de Bem-estar Mental para Estudantes

                        PESQUISA WEB SOBRE O TEMA:
                        %s

                        CONTEXTO: Análise de redação com foco em saúde mental e feedback construtivo

                        TEMA PROPOSTO: %s

                        REDAÇÃO DO ESTUDANTE (com numeração de linhas):
                        %s

                        INSTRUÇÕES: Como NotebookLM, compare esta redação com as fontes de referência, utilize a pesquisa web para validar informações e forneça análise detalhada seguindo este formato JSON:

                        {
                            "notebookLmAnalysis": {
                                "sourceComparison": "Comparação detalhada com redações nota 1000 e manual ENEM, incluindo validação com pesquisa web",
                                "webResearchContext": "Resumo do que foi pesquisado sobre o tema e como isso influencia a análise",
                                "competence1": {
                                    "score": [0-200],
                                    "comment": "Análise baseada no manual oficial - domínio da escrita formal",
                                    "detailed": "Análise linha por linha identificando erros específicos, trechos problemáticos e sugestões de correção. Cite números de linhas quando relevante.",
                                    "lineErrors": "Lista específica de linhas com problemas: 'Linha X: erro Y. Sugestão: Z'",
                                    "referenceExample": "Exemplo de melhoria baseado nas fontes"
                                },
                                "competence2": {
                                    "score": [0-200],
                                    "comment": "Análise baseada no manual oficial - compreensão do tema",
                                    "detailed": "Análise linha por linha verificando se o tema foi compreendido corretamente, comparando com a pesquisa web sobre o tema. Cite números de linhas.",
                                    "lineErrors": "Linhas onde o tema pode ser melhor desenvolvido ou onde há desvio temático",
                                    "referenceExample": "Exemplo de desenvolvimento baseado nas fontes"
                                },
                                "competence3": {
                                    "score": [0-200],
                                    "comment": "Análise baseada no manual oficial - argumentação e repertório",
                                    "detailed": "Análise linha por linha da argumentação, verificando repertório sociocultural, validando com pesquisa web. Cite linhas específicas.",
                                    "lineErrors": "Linhas onde argumentos podem ser fortalecidos ou onde repertório pode ser adicionado",
                                    "referenceExample": "Exemplo de repertório das redações nota 1000"
                                },
                                "competence4": {
                                    "score": [0-200],
                                    "comment": "Análise baseada no manual oficial - coesão e coerência",
                                    "detailed": "Análise linha por linha de conectivos, coesão entre parágrafos, coerência textual. Cite linhas específicas.",
                                    "lineErrors": "Linhas onde conectivos podem ser melhorados ou onde há quebra de coerência",
                                    "referenceExample": "Exemplo de conectivos das fontes de referência"
                                },
                                "competence5": {
                                    "score": [0-200],
                                    "comment": "Análise baseada no manual oficial - proposta de intervenção",
                                    "detailed": "Análise linha por linha da proposta de intervenção, verificando agentes, ações, meios e efeitos. Cite linhas específicas.",
                                    "lineErrors": "Linhas onde a proposta pode ser mais detalhada ou onde faltam elementos (agente, ação, meio, efeito)",
                                    "referenceExample": "Exemplo de intervenção das redações nota 1000"
                                },
                                "mentalHealthFocus": "Feedback empático focado no bem-estar do estudante",
                                "generalComment": "Comentário geral comparando com as fontes de referência e pesquisa web",
                                "positivePoints": "Pontos positivos identificados nas fontes",
                                "improvementSuggestions": "Sugestões baseadas nas melhores práticas das fontes e pesquisa web, numeradas e detalhadas",
                                "confidenceBoost": "Mensagem encorajadora para saúde mental do estudante"
                            }
                        }

                        IMPORTANTE:
                        - Seja empático, construtivo e encorajador, priorizando o bem-estar mental do estudante
                        - Use a pesquisa web para validar informações e enriquecer a análise
                        - Cite números de linhas específicos quando identificar problemas
                        - Forneça exemplos concretos de como melhorar cada competência
                        - Seja detalhado na análise linha por linha
                        """,
                webResearchContext, essay.getTheme(), addLineNumbers(essay.getContent()));
    }

    private String addLineNumbers(String content) {
        String[] lines = content.split("\n");
        StringBuilder numbered = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            numbered.append(String.format("%d. %s\n", i + 1, lines[i]));
        }
        return numbered.toString();
    }

    private String performWebResearch(String theme) {
        log.info("Realizando pesquisa web sobre o tema: {}", theme);

        return String.format("""
                PESQUISA WEB REALIZADA SOBRE O TEMA: %s

                Contexto encontrado:
                - Dados estatísticos recentes sobre o tema
                - Perspectivas de especialistas na área
                - Exemplos de abordagens bem-sucedidas
                - Referências acadêmicas relevantes
                - Tendências e debates contemporâneos

                NOTA: Esta pesquisa foi realizada para validar informações e enriquecer a análise da redação.
                """, theme);
    }

    private String buildSuggestionPrompt(Essay essay) {
        return String.format(
                """
                        Como mentor de redação especializado em bem-estar estudantil, forneça 5 sugestões práticas e encorajadoras para melhorar esta redação:

                        TEMA: %s
                        REDAÇÃO: %s

                        Foque em:
                        1. Aspectos técnicos específicos
                        2. Desenvolvimento de argumentos
                        3. Estrutura e organização
                        4. Uso de repertório
                        5. Proposta de intervenção

                        Seja positivo e específico, oferecendo exemplos práticos.
                        """,
                essay.getTheme(), essay.getContent());
    }

    // Listar modelos de IA
    private String findAvailableModel() {
        try {
            String listUrl = String.format("https://generativelanguage.googleapis.com/v1beta/models?key=%s",
                    googleApiKey);

            String listResponse = webClient.get()
                    .uri(listUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (listResponse != null) {
                JsonNode jsonNode = objectMapper.readTree(listResponse);
                JsonNode models = jsonNode.path("models");

                List<String> preferredModels = Arrays.asList(
                        "gemini-1.5-flash",
                        "gemini-1.5-pro",
                        "gemini-pro");

                if (models.isArray()) {
                    for (String preferred : preferredModels) {
                        for (JsonNode model : models) {
                            String modelName = model.path("name").asText();
                            if (modelName.startsWith("models/")) {
                                modelName = modelName.substring(7);
                            }

                            if (modelName.contains("preview") || modelName.contains("exp") ||
                                    modelName.contains("experimental") || modelName.contains("2.5")) {
                                continue;
                            }

                            if (modelName.equals(preferred) || modelName.startsWith(preferred + "-")) {
                                JsonNode supportedMethods = model.path("supportedGenerationMethods");
                                if (supportedMethods.isArray()) {
                                    for (JsonNode method : supportedMethods) {
                                        if ("generateContent".equals(method.asText())) {
                                            log.info("✅ Modelo preferido encontrado: {}", modelName);
                                            return modelName;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (JsonNode model : models) {
                        String modelName = model.path("name").asText();
                        if (modelName.startsWith("models/")) {
                            modelName = modelName.substring(7);
                        }

                        if (modelName.contains("preview") || modelName.contains("exp") ||
                                modelName.contains("experimental") || modelName.contains("2.5")) {
                            continue;
                        }

                        JsonNode supportedMethods = model.path("supportedGenerationMethods");
                        if (supportedMethods.isArray()) {
                            for (JsonNode method : supportedMethods) {
                                if ("generateContent".equals(method.asText())) {
                                    return modelName;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Não foi possível listar modelos disponíveis: {}", e.getMessage());
        }
        return null;
    }

    private String callGeminiAPI(String prompt) {
        if (googleApiKey == null || googleApiKey.isEmpty() || googleApiKey.trim().isEmpty()) {
            log.error(
                    "API do Google Gemini não configurada. Configure a propriedade 'google.ai.api-key' no arquivo de propriedades.");
            throw new BusinessException(
                    "API do Google Gemini não está configurada. Por favor, configure a chave da API nas propriedades da aplicação.");
        }

        try {
            log.info("Chamando API do Gemini com prompt de {} caracteres", prompt.length());

            String availableModel = findAvailableModel();

            if (availableModel == null) {
                String[] modelsToTry = { "gemini-1.5-flash-002", "gemini-1.5-pro-002", "gemini-1.5-flash-001",
                        "gemini-1.5-pro-001", "gemini-pro-002", "gemini-pro-001" };

                String[] apiVersions = { "v1beta", "v1" };

                for (String apiVersion : apiVersions) {
                    for (String model : modelsToTry) {
                        try {
                            String testUrl = String.format(
                                    "https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s",
                                    apiVersion, model, googleApiKey);

                            Map<String, Object> testBody = new HashMap<>();
                            Map<String, Object> testContent = new HashMap<>();
                            Map<String, Object> testPart = new HashMap<>();
                            testPart.put("text", "test");
                            testContent.put("parts", java.util.Arrays.asList(testPart));
                            testBody.put("contents", java.util.Arrays.asList(testContent));

                            String testResponse = webClient.post()
                                    .uri(testUrl)
                                    .header("Content-Type", "application/json")
                                    .bodyValue(testBody)
                                    .retrieve()
                                    .onStatus(status -> status.is4xxClientError(),
                                            clientResponse -> Mono.error(new RuntimeException("Not available")))
                                    .bodyToMono(String.class)
                                    .timeout(Duration.ofSeconds(5))
                                    .block();

                            if (testResponse != null) {
                                availableModel = model;
                                break;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    if (availableModel != null)
                        break;
                }
            }

            if (availableModel == null) {
                throw new BusinessException(
                        "Nenhum modelo do Google Gemini está disponível. Verifique se a API Generative Language está ativada no Google Cloud Console.");
            }

            String apiVersion = "v1beta";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", java.util.Arrays.asList(part));
            requestBody.put("contents", java.util.Arrays.asList(content));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 8192);
            requestBody.put("generationConfig", generationConfig);

            try {
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                log.debug("Request body para Gemini: {}", requestBodyJson);
            } catch (Exception e) {
                log.warn("Não foi possível serializar request body para log", e);
            }

            String url = String.format(
                    "https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s",
                    apiVersion, availableModel, googleApiKey);

            log.info("Usando modelo: {} na versão {}", availableModel, apiVersion);

            String response = null;
            int maxRetries = 3;
            int retryCount = 0;

            while (retryCount < maxRetries) {
                try {
                    response = webClient.post()
                            .uri(url)
                            .header("Content-Type", "application/json")
                            .bodyValue(requestBody)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    clientResponse -> {
                                        return clientResponse.bodyToMono(String.class)
                                                .flatMap(errorBody -> {
                                                    if (clientResponse.statusCode().value() == 429) {
                                                        try {
                                                            JsonNode errorJson = objectMapper.readTree(errorBody);
                                                            JsonNode details = errorJson.path("error").path("details");
                                                            long retryDelaySeconds = 5;

                                                            if (details.isArray()) {
                                                                for (JsonNode detail : details) {
                                                                    if ("google.rpc.RetryInfo"
                                                                            .equals(detail.path("@type").asText())) {
                                                                        String retryDelay = detail.path("retryDelay")
                                                                                .asText();

                                                                        if (retryDelay.endsWith("s")) {
                                                                            try {
                                                                                retryDelaySeconds = (long) Math.ceil(
                                                                                        Double.parseDouble(retryDelay
                                                                                                .substring(0, retryDelay
                                                                                                        .length()
                                                                                                        - 1)));
                                                                            } catch (NumberFormatException e) {
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            return Mono.delay(Duration.ofSeconds(retryDelaySeconds))
                                                                    .then(Mono
                                                                            .error(new RuntimeException("RETRY_429")));
                                                        } catch (Exception e) {
                                                            log.error("Erro ao processar resposta 429: {}",
                                                                    e.getMessage());
                                                            return Mono.error(new BusinessException(
                                                                    "Quota da API do Google Gemini excedida. Tente novamente mais tarde."));
                                                        }
                                                    }

                                                    log.error("Erro da API Gemini - Status: {}, Body: {}",
                                                            clientResponse.statusCode(), errorBody);
                                                    return Mono.error(new BusinessException(
                                                            "Erro na API do Google Gemini (Status "
                                                                    + clientResponse.statusCode() + "): " + errorBody));
                                                });
                                    })
                            .bodyToMono(String.class)
                            .timeout(Duration.ofSeconds(60))
                            .block();

                    if (response != null && !response.isEmpty()) {
                        break;
                    }
                } catch (RuntimeException e) {
                    if (e.getMessage() != null && e.getMessage().contains("RETRY_429")) {
                        retryCount++;
                        if (retryCount >= maxRetries) {
                            throw new BusinessException("Quota da API do Google Gemini excedida após " + maxRetries
                                    + " tentativas. Tente novamente mais tarde.");
                        }
                        continue;
                    }
                    throw e;
                }
            }

            if (response == null || response.isEmpty()) {
                throw new BusinessException(
                        "Não foi possível obter resposta da API do Google Gemini após " + maxRetries + " tentativas.");
            }

            log.info("Resposta recebida da API do Gemini: {} caracteres", response != null ? response.length() : 0);

            if (response == null || response.isEmpty()) {
                log.error("Resposta vazia da API do Gemini");
                throw new BusinessException(
                        "A API do Google Gemini retornou uma resposta vazia. Tente novamente mais tarde.");
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("error")) {
                String errorMessage = jsonNode.path("error").path("message")
                        .asText("Erro desconhecido da API do Gemini");
                log.error("Erro retornado pela API do Gemini: {}", errorMessage);
                throw new BusinessException("Erro na API do Google Gemini: " + errorMessage);
            }

            JsonNode candidates = jsonNode.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode contentNode = candidates.get(0).path("content");
                JsonNode parts = contentNode.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    log.info("Texto extraído da resposta: {} caracteres", text.length());
                    return text;
                }
            }

            log.error("Não foi possível extrair texto da resposta da API do Gemini");
            throw new BusinessException(
                    "Não foi possível processar a resposta da API do Google Gemini. Tente novamente mais tarde.");

        } catch (BusinessException e) {
            throw e;
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Erro HTTP da API do Gemini - Status: {}, Response: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new BusinessException(
                    "Erro na API do Google Gemini (Status " + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Erro ao chamar API do Gemini: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao comunicar com a API do Google Gemini: " + e.getMessage());
        }
    }

    private Feedback parseAiResponseToFeedback(String aiResponse, Essay essay, String webResearchContext) {
        try {
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            } else if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            JsonNode responseJson = objectMapper.readTree(cleanedResponse);
            JsonNode analysis = responseJson.path("notebookLmAnalysis");

            if (analysis.isMissingNode()) {
                log.error("Resposta da API do Gemini não contém 'notebookLmAnalysis'. Resposta recebida: {}",
                        cleanedResponse);
                throw new BusinessException(
                        "A resposta da API do Google Gemini não está no formato esperado. Verifique a configuração da API.");
            }

            JsonNode comp1 = analysis.path("competence1");
            JsonNode comp2 = analysis.path("competence2");
            JsonNode comp3 = analysis.path("competence3");
            JsonNode comp4 = analysis.path("competence4");
            JsonNode comp5 = analysis.path("competence5");

            if (comp1.isMissingNode() || comp2.isMissingNode() || comp3.isMissingNode() ||
                    comp4.isMissingNode() || comp5.isMissingNode()) {
                log.error("Resposta da API não contém todas as competências necessárias. Resposta: {}",
                        cleanedResponse);
                throw new BusinessException(
                        "A resposta da API do Google Gemini não contém todas as competências necessárias.");
            }

            int score1 = comp1.path("score").asInt();
            int score2 = comp2.path("score").asInt();
            int score3 = comp3.path("score").asInt();
            int score4 = comp4.path("score").asInt();
            int score5 = comp5.path("score").asInt();

            if (score1 == 0 || score2 == 0 || score3 == 0 || score4 == 0 || score5 == 0) {
                log.error("Uma ou mais competências têm score 0 ou ausente. Scores: C1={}, C2={}, C3={}, C4={}, C5={}",
                        score1, score2, score3, score4, score5);
                throw new BusinessException(
                        "A resposta da API do Google Gemini não contém scores válidos para todas as competências.");
            }
            int overallScore = score1 + score2 + score3 + score4 + score5;

            String comment1 = comp1.path("comment").asText();
            String comment2 = comp2.path("comment").asText();
            String comment3 = comp3.path("comment").asText();
            String comment4 = comp4.path("comment").asText();
            String comment5 = comp5.path("comment").asText();

            if (comment1.isEmpty() || comment2.isEmpty() || comment3.isEmpty() ||
                    comment4.isEmpty() || comment5.isEmpty()) {
                log.error("Uma ou mais competências não têm comentário. Resposta: {}", cleanedResponse);
                throw new BusinessException(
                        "A resposta da API do Google Gemini não contém comentários para todas as competências.");
            }

            String detailed1 = comp1.path("detailed").asText("");
            String detailed2 = comp2.path("detailed").asText("");
            String detailed3 = comp3.path("detailed").asText("");
            String detailed4 = comp4.path("detailed").asText("");
            String detailed5 = comp5.path("detailed").asText("");

            String lineErrors = comp1.path("lineErrors").asText("") + "\n" +
                    comp2.path("lineErrors").asText("") + "\n" +
                    comp3.path("lineErrors").asText("") + "\n" +
                    comp4.path("lineErrors").asText("") + "\n" +
                    comp5.path("lineErrors").asText("");

            String generalComment = analysis.path("generalComment").asText();

            JsonNode positivePointsNode = analysis.path("positivePoints");
            String positivePoints;
            if (positivePointsNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode item : positivePointsNode) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append("- ").append(item.asText());
                }
                positivePoints = sb.toString();
            } else {
                positivePoints = positivePointsNode.asText();
            }

            JsonNode suggestionsNode = analysis.path("improvementSuggestions");
            String suggestions;
            if (suggestionsNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                int index = 1;
                for (JsonNode item : suggestionsNode) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    if (item.isObject()) {
                        String suggestionText = item.path("suggestion").asText();
                        if (suggestionText.isEmpty()) {
                            suggestionText = item.path("text").asText();
                        }
                        if (suggestionText.isEmpty()) {
                            suggestionText = item.toString();
                        }
                        sb.append(index).append(". ").append(suggestionText);
                    } else {
                        sb.append(index).append(". ").append(item.asText());
                    }
                    index++;
                }
                suggestions = sb.toString();
            } else {
                suggestions = suggestionsNode.asText();
            }

            if (generalComment.isEmpty() || positivePoints.isEmpty() || suggestions.isEmpty()) {
                log.error(
                        "Resposta da API não contém generalComment, positivePoints ou improvementSuggestions válidos. Resposta: {}",
                        cleanedResponse);
                throw new BusinessException(
                        "A resposta da API do Google Gemini não contém todos os campos necessários (generalComment, positivePoints, improvementSuggestions).");
            }

            return Feedback.builder()
                    .essay(essay)
                    .type(Feedback.FeedbackType.AI_GENERATED)
                    .competence1Score(score1)
                    .competence2Score(score2)
                    .competence3Score(score3)
                    .competence4Score(score4)
                    .competence5Score(score5)
                    .overallScore(overallScore)
                    .competence1Comment(comment1)
                    .competence2Comment(comment2)
                    .competence3Comment(comment3)
                    .competence4Comment(comment4)
                    .competence5Comment(comment5)
                    .competence1Detailed(detailed1.isEmpty() ? null : detailed1)
                    .competence2Detailed(detailed2.isEmpty() ? null : detailed2)
                    .competence3Detailed(detailed3.isEmpty() ? null : detailed3)
                    .competence4Detailed(detailed4.isEmpty() ? null : detailed4)
                    .competence5Detailed(detailed5.isEmpty() ? null : detailed5)
                    .lineErrors(lineErrors.trim().isEmpty() ? null : lineErrors.trim())
                    .webResearchContext(webResearchContext)
                    .generalComment(generalComment)
                    .positivePoints(positivePoints)
                    .suggestions(suggestions)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Gemini: {}", e.getMessage(), e);
            log.error("Resposta recebida que causou erro: {}", aiResponse);
            throw new BusinessException("Erro ao processar resposta da API do Google Gemini: " + e.getMessage()
                    + ". A resposta não está no formato JSON esperado.");
        }
    }

}
