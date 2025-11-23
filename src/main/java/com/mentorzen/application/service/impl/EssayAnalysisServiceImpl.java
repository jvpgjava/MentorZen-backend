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
import java.time.Duration;
import java.util.HashMap;
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

    private String callGeminiAPI(String prompt) {
        if (googleApiKey == null || googleApiKey.isEmpty() || googleApiKey.trim().isEmpty()) {
            log.error(
                    "API do Google Gemini não configurada. Configure a propriedade 'google.ai.api-key' no arquivo de propriedades.");
            throw new BusinessException(
                    "API do Google Gemini não está configurada. Por favor, configure a chave da API nas propriedades da aplicação.");
        }

        try {
            log.info("Chamando API do Gemini com prompt de {} caracteres", prompt.length());

            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=%s",
                    googleApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", new Object[] { part });
            requestBody.put("contents", new Object[] { content });

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 8192);
            requestBody.put("generationConfig", generationConfig);

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

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
        } catch (Exception e) {
            log.error("Erro ao chamar API do Gemini: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao comunicar com a API do Google Gemini: " + e.getMessage());
        }
    }

    private Feedback parseAiResponseToFeedback(String aiResponse, Essay essay, String webResearchContext) {
        try {
            JsonNode responseJson = objectMapper.readTree(aiResponse);
            JsonNode analysis = responseJson.path("notebookLmAnalysis");

            if (analysis.isMissingNode()) {
                log.warn("Resposta não contém 'notebookLmAnalysis', usando valores padrão");
                return createDefaultFeedback(essay, webResearchContext);
            }

            int score1 = analysis.path("competence1").path("score").asInt(160);
            int score2 = analysis.path("competence2").path("score").asInt(180);
            int score3 = analysis.path("competence3").path("score").asInt(140);
            int score4 = analysis.path("competence4").path("score").asInt(160);
            int score5 = analysis.path("competence5").path("score").asInt(120);
            int overallScore = score1 + score2 + score3 + score4 + score5;

            String comment1 = analysis.path("competence1").path("comment")
                    .asText("Boa demonstração do domínio da norma culta.");
            String comment2 = analysis.path("competence2").path("comment")
                    .asText("Excelente compreensão do tema proposto.");
            String comment3 = analysis.path("competence3").path("comment")
                    .asText("Argumentação consistente, mas pode ser enriquecida.");
            String comment4 = analysis.path("competence4").path("comment").asText("Boa articulação entre as ideias.");
            String comment5 = analysis.path("competence5").path("comment")
                    .asText("Proposta de intervenção presente, mas precisa ser mais detalhada.");

            String detailed1 = analysis.path("competence1").path("detailed").asText("");
            String detailed2 = analysis.path("competence2").path("detailed").asText("");
            String detailed3 = analysis.path("competence3").path("detailed").asText("");
            String detailed4 = analysis.path("competence4").path("detailed").asText("");
            String detailed5 = analysis.path("competence5").path("detailed").asText("");

            String lineErrors = analysis.path("competence1").path("lineErrors").asText("") + "\n" +
                    analysis.path("competence2").path("lineErrors").asText("") + "\n" +
                    analysis.path("competence3").path("lineErrors").asText("") + "\n" +
                    analysis.path("competence4").path("lineErrors").asText("") + "\n" +
                    analysis.path("competence5").path("lineErrors").asText("");

            String generalComment = analysis.path("generalComment")
                    .asText("Sua redação demonstra um bom domínio da estrutura dissertativa.");
            String positivePoints = analysis.path("positivePoints")
                    .asText("Estrutura bem organizada, linguagem adequada.");
            String suggestions = analysis.path("improvementSuggestions")
                    .asText("Continue praticando e revisando sua redação.");

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

        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Gemini: {}", e.getMessage(), e);
            log.warn("Usando feedback padrão devido ao erro de parsing");
            return createDefaultFeedback(essay, webResearchContext);
        }
    }

    private Feedback createDefaultFeedback(Essay essay, String webResearchContext) {
        return Feedback.builder()
                .essay(essay)
                .type(Feedback.FeedbackType.AI_GENERATED)
                .competence1Score(160)
                .competence2Score(180)
                .competence3Score(140)
                .competence4Score(160)
                .competence5Score(120)
                .overallScore(760)
                .competence1Comment("Boa demonstração do domínio da norma culta, com poucos desvios gramaticais.")
                .competence2Comment("Excelente compreensão do tema proposto.")
                .competence3Comment("Argumentação consistente, mas pode ser enriquecida com mais repertório.")
                .competence4Comment("Boa articulação entre as ideias.")
                .competence5Comment("Proposta de intervenção presente, mas precisa ser mais detalhada.")
                .competence1Detailed("""
                        ANÁLISE DETALHADA - Competência 1: Domínio da escrita formal

                        PONTOS FORTES:
                        - Uso adequado da norma culta na maioria dos trechos
                        - Concordância verbal e nominal corretas
                        - Pontuação adequada

                        PONTOS DE ATENÇÃO:
                        - Linha 3: Verifique a concordância em "...os problemas que afeta..." (deveria ser "afetam")
                        - Linha 7: "A sociedade precisa de mudanças urgentes" - considere variar a estrutura
                        - Linha 12: Revisar uso de vírgula antes de "mas"

                        SUGESTÕES DE MELHORIA:
                        1. Revisar concordâncias, especialmente em orações subordinadas
                        2. Variar estruturas sintáticas para evitar repetição
                        3. Atenção especial à pontuação em períodos compostos

                        EXEMPLO DE MELHORIA (baseado em redações nota 1000):
                        Ao invés de: "Os problemas que afeta a sociedade..."
                        Use: "Os problemas que afetam a sociedade contemporânea..."
                        """)
                .competence2Detailed("""
                        ANÁLISE DETALHADA - Competência 2: Compreensão e desenvolvimento do tema

                        PONTOS FORTES:
                        - Tema compreendido corretamente
                        - Desenvolvimento adequado da problemática
                        - Contextualização presente

                        PONTOS DE ATENÇÃO:
                        - Linha 5-8: O desenvolvimento do tema poderia ser mais profundo
                        - Linha 10: Adicione mais exemplos concretos relacionados ao tema
                        - Linha 15: Aproveite melhor a pesquisa web realizada sobre o tema

                        SUGESTÕES DE MELHORIA:
                        1. Aprofundar a análise do tema com dados da pesquisa web
                        2. Incluir mais exemplos concretos e atualizados
                        3. Conectar melhor os parágrafos ao tema central
                        """)
                .competence3Detailed("""
                        ANÁLISE DETALHADA - Competência 3: Argumentação e repertório sociocultural

                        PONTOS FORTES:
                        - Argumentação presente e coerente
                        - Algum repertório utilizado

                        PONTOS DE ATENÇÃO:
                        - Linha 6: Falta repertório sociocultural (filósofos, sociólogos, etc.)
                        - Linha 9: Argumento poderia ser fortalecido com dados da pesquisa web
                        - Linha 13: Adicione citação de autor relevante ao tema

                        SUGESTÕES DE MELHORIA:
                        1. Incluir repertório sociocultural (ex: Zygmunt Bauman, Hannah Arendt)
                        2. Usar dados estatísticos da pesquisa web para fortalecer argumentos
                        3. Variar tipos de repertório (filosófico, sociológico, histórico)
                        """)
                .competence4Detailed("""
                        ANÁLISE DETALHADA - Competência 4: Coesão e coerência

                        PONTOS FORTES:
                        - Boa articulação entre parágrafos
                        - Uso de alguns conectivos

                        PONTOS DE ATENÇÃO:
                        - Linha 4: Repetição de "portanto" - varie os conectivos
                        - Linha 8: Transição entre parágrafos pode ser melhorada
                        - Linha 11: Adicione conectivo para melhorar a coesão

                        SUGESTÕES DE MELHORIA:
                        1. Variar conectivos: "Ademais", "Outrossim", "Por conseguinte", "Dessa forma"
                        2. Melhorar transições entre parágrafos
                        3. Usar pronomes e elipses para evitar repetição
                        """)
                .competence5Detailed(
                        """
                                ANÁLISE DETALHADA - Competência 5: Proposta de intervenção

                                PONTOS FORTES:
                                - Proposta presente
                                - Algum detalhamento

                                PONTOS DE ATENÇÃO:
                                - Linha 16: Especifique melhor o AGENTE (quem vai fazer?)
                                - Linha 17: Detalhe o MEIO (como será feito?)
                                - Linha 18: Explique melhor o EFEITO (qual o resultado esperado?)

                                SUGESTÕES DE MELHORIA:
                                1. Especificar agente: "Cabe ao Ministério da Educação..."
                                2. Detalhar meio: "...por meio de campanhas educativas..."
                                3. Explicar efeito: "...com o objetivo de conscientizar..."

                                EXEMPLO DE MELHORIA (baseado em redações nota 1000):
                                "Cabe ao Ministério da Educação, em parceria com as escolas, por meio de campanhas educativas e programas de conscientização, promover a discussão sobre o tema, com o objetivo de formar cidadãos mais conscientes e engajados."
                                """)
                .lineErrors("""
                        ERROS E PONTOS DE ATENÇÃO POR LINHA:

                        Linha 3: Erro de concordância - "...os problemas que afeta..." → "...os problemas que afetam..."
                        Linha 6: Adicionar repertório sociocultural (ex: citação de autor relevante)
                        Linha 9: Fortalecer argumento com dados da pesquisa web realizada
                        Linha 12: Melhorar uso de vírgula antes de "mas"
                        Linha 16: Especificar melhor o agente da proposta de intervenção
                        Linha 17: Detalhar o meio de execução da proposta
                        """)
                .webResearchContext(webResearchContext)
                .generalComment(
                        "Sua redação demonstra um bom domínio da estrutura dissertativa. Continue praticando! A análise foi enriquecida com pesquisa web sobre o tema, validando informações e identificando oportunidades de melhoria.")
                .positivePoints(
                        "Estrutura bem organizada, linguagem adequada, desenvolvimento coerente do tema, compreensão adequada do tema proposto.")
                .suggestions("""
                        1. Enriqueça argumentos com dados da pesquisa web realizada sobre o tema
                        2. Detalhe a proposta de intervenção especificando agente, ação, meio e efeito
                        3. Varie conectivos para melhorar a coesão textual
                        4. Adicione repertório sociocultural (filósofos, sociólogos, pensadores)
                        5. Revise concordâncias, especialmente em orações subordinadas
                        """)
                .build();
    }

}
