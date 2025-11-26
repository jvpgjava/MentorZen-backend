package com.mentorzen.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.mentorzen.application.service.EssayAnalysisService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import com.mentorzen.infrastructure.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EssayAnalysisServiceImpl implements EssayAnalysisService {

    @Value("${google.ai.api-key:}")
    private String apiKey;

    @Value("${google.ai.model:gemini-2.0-flash-lite}")
    private String modelName;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Client client;

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

    private Client getClient() {
        if (client == null) {
            validateApiKey();
            client = Client.builder().apiKey(apiKey).build();
            log.info("Client do Google Gemini inicializado com sucesso");
        }
        return client;
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("API Key do Google Gemini não configurada");
            throw new BusinessException(
                    "API Key do Google Gemini não está configurada. Configure a propriedade 'google.ai.api-key'.");
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            log.info("Chamando API do Gemini com prompt de {} caracteres usando modelo: {}",
                    prompt.length(), modelName);

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .topK(40f)
                    .topP(0.95f)
                    .maxOutputTokens(8192)
                    .build();

            GenerateContentResponse response = getClient().models.generateContent(
                    modelName,
                    prompt,
                    config
            );

            if (response == null) {
                throw new BusinessException("A API do Google Gemini retornou uma resposta nula.");
            }

            String text = response.text();
            log.info("Texto extraído da resposta: {} caracteres", text.length());
            return text;

        } catch (BusinessException e) {
            throw e;
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
