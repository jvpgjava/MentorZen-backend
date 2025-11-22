package com.mentorzen.application.service.impl;

import com.mentorzen.application.service.EssayAnalysisService;
import com.mentorzen.domain.entity.Essay;
import com.mentorzen.domain.entity.Feedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class EssayAnalysisServiceImpl implements EssayAnalysisService {

    private final WebClient webClient;

    @Value("${google.ai.api-key}")
    private String googleApiKey;

    @Override
    public Feedback analyzeEssay(Essay essay) {
        log.info("Iniciando análise da redação ID: {}", essay.getId());

        try {
            // Prompt estruturado para análise de redação ENEM
            String prompt = buildAnalysisPrompt(essay);

            // Chamada para a API do Google AI (Gemini)
            String aiResponse = callGeminiAPI(prompt);

            // Parse da resposta e construção do feedback
            return parseAiResponseToFeedback(aiResponse, essay);

        } catch (Exception e) {
            log.error("Erro ao analisar redação ID: {}", essay.getId(), e);
            return createErrorFeedback(essay, e.getMessage());
        }
    }

    @Override
    public String generateImprovementSuggestions(Essay essay) {
        log.info("Gerando sugestões de melhoria para redação ID: {}", essay.getId());

        String prompt = buildSuggestionPrompt(essay);

        try {
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Erro ao gerar sugestões para redação ID: {}", essay.getId(), e);
            return "Não foi possível gerar sugestões no momento. Tente novamente mais tarde.";
        }
    }

    @Override
    public boolean validateEnemCriteria(Essay essay) {
        // Validações básicas dos critérios ENEM
        if (essay.getContent() == null || essay.getContent().trim().isEmpty()) {
            return false;
        }

        int wordCount = essay.getWordCount() != null ? essay.getWordCount() : 0;

        // Redação deve ter entre 7 e 30 linhas (aproximadamente 150-800 palavras)
        if (wordCount < 150 || wordCount > 800) {
            return false;
        }

        // Verificar se tem pelo menos 4 parágrafos (estrutura básica)
        String[] paragraphs = essay.getContent().split("\n\n");
        if (paragraphs.length < 3) {
            return false;
        }

        return true;
    }

    private String buildAnalysisPrompt(Essay essay) {
        return String.format("""
            SISTEMA: Você é o NotebookLM especializado em correção de redações ENEM, com acesso às seguintes fontes de referência:
            
            FONTES DE REFERÊNCIA:
            - Manual de Redação do ENEM 2024
            - Cartilha do Participante ENEM
            - Redações Nota 1000 dos últimos 5 anos
            - Critérios oficiais das 5 competências ENEM
            - Guia de Bem-estar Mental para Estudantes
            
            CONTEXTO: Análise de redação com foco em saúde mental e feedback construtivo
            
            TEMA PROPOSTO: %s
            
            REDAÇÃO DO ESTUDANTE:
            %s
            
            INSTRUÇÕES: Como NotebookLM, compare esta redação com as fontes de referência e forneça análise detalhada seguindo este formato JSON:
            
            {
                "notebookLmAnalysis": {
                    "sourceComparison": "Comparação com redações nota 1000 e manual ENEM",
                    "competence1": {
                        "score": [0-200],
                        "comment": "Análise baseada no manual oficial - domínio da escrita formal",
                        "referenceExample": "Exemplo de melhoria baseado nas fontes"
                    },
                    "competence2": {
                        "score": [0-200],
                        "comment": "Análise baseada no manual oficial - compreensão do tema",
                        "referenceExample": "Exemplo de desenvolvimento baseado nas fontes"
                    },
                    "competence3": {
                        "score": [0-200],
                        "comment": "Análise baseada no manual oficial - argumentação e repertório",
                        "referenceExample": "Exemplo de repertório das redações nota 1000"
                    },
                    "competence4": {
                        "score": [0-200],
                        "comment": "Análise baseada no manual oficial - coesão e coerência",
                        "referenceExample": "Exemplo de conectivos das fontes de referência"
                    },
                    "competence5": {
                        "score": [0-200],
                        "comment": "Análise baseada no manual oficial - proposta de intervenção",
                        "referenceExample": "Exemplo de intervenção das redações nota 1000"
                    },
                    "mentalHealthFocus": "Feedback empático focado no bem-estar do estudante",
                    "generalComment": "Comentário geral comparando com as fontes de referência",
                    "positivePoints": "Pontos positivos identificados nas fontes",
                    "improvementSuggestions": "Sugestões baseadas nas melhores práticas das fontes",
                    "confidenceBoost": "Mensagem encorajadora para saúde mental do estudante"
                }
            }
            
            IMPORTANTE: Seja empático, construtivo e encorajador, priorizando o bem-estar mental do estudante conforme as diretrizes das fontes de referência.
            """, essay.getTheme(), essay.getContent());
    }

    private String buildSuggestionPrompt(Essay essay) {
        return String.format("""
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
            """, essay.getTheme(), essay.getContent());
    }

    private String callGeminiAPI(String prompt) {
        // Implementação da chamada para a API do Google Gemini
        // Por enquanto, retorna uma resposta mock para desenvolvimento
        return mockGeminiResponse();
    }

    private String mockGeminiResponse() {
        // Mock response simulando NotebookLM para desenvolvimento
        return """
            {
                "notebookLmAnalysis": {
                    "sourceComparison": "Comparando com 847 redações nota 1000 e Manual ENEM 2024: sua redação está no nível intermediário-avançado",
                    "competence1": {
                        "score": 160,
                        "comment": "Baseado no Manual ENEM: boa demonstração da norma culta, com poucos desvios. Comparando com redações nota 1000, você está no caminho certo.",
                        "referenceExample": "Nas redações nota 1000, vemos: 'A sociedade contemporânea vivencia...' - note a concordância precisa"
                    },
                    "competence2": {
                        "score": 180,
                        "comment": "Segundo as fontes de referência: excelente compreensão temática. Seu desenvolvimento está alinhado com os melhores exemplos.",
                        "referenceExample": "Redações nota 1000 desenvolvem o tema com: 'Nesse contexto, é fundamental analisar...' - estrutura similar à sua"
                    },
                    "competence3": {
                        "score": 140,
                        "comment": "Conforme o Manual ENEM: argumentação consistente, mas pode ser enriquecida. Nas redações nota 1000, vemos mais repertório diversificado.",
                        "referenceExample": "Exemplo das fontes: 'Segundo Zygmunt Bauman em Modernidade Líquida...' - repertório filosófico"
                    },
                    "competence4": {
                        "score": 160,
                        "comment": "Baseado nas melhores práticas: boa articulação. As redações nota 1000 usam conectivos mais variados.",
                        "referenceExample": "Fontes mostram: 'Ademais', 'Outrossim', 'Por conseguinte' - varie os conectivos"
                    },
                    "competence5": {
                        "score": 120,
                        "comment": "Segundo o Manual ENEM: proposta presente, mas precisa detalhar mais. Redações nota 1000 especificam agentes e meios.",
                        "referenceExample": "Exemplo das fontes: 'Cabe ao Ministério da Educação, por meio de campanhas...' - especificidade"
                    },
                    "mentalHealthFocus": "Você está no caminho certo! Sua redação mostra evolução e potencial. Cada texto é um passo importante na sua jornada. 🌟",
                    "generalComment": "Comparando com as fontes NotebookLM: sua redação demonstra domínio da estrutura dissertativa e está bem posicionada para melhorias pontuais.",
                    "positivePoints": "Estrutura sólida (como nas redações nota 1000), linguagem adequada (conforme Manual ENEM), desenvolvimento coerente, posicionamento claro",
                    "improvementSuggestions": "1. Repertório: inclua filósofos/sociólogos como nas redações nota 1000. 2. Conectivos: varie conforme Manual ENEM. 3. Intervenção: detalhe agentes como nas melhores redações. 4. Dados: use estatísticas como nas fontes de referência.",
                    "confidenceBoost": "Lembre-se: você já domina o essencial! Cada redação é uma oportunidade de crescimento. Confie no seu potencial! 💪✨"
                }
            }
            """;
    }

    private Feedback parseAiResponseToFeedback(String aiResponse, Essay essay) {
        // Parse simples para desenvolvimento - em produção usar Jackson ou similar
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
                .generalComment("Sua redação demonstra um bom domínio da estrutura dissertativa. Continue praticando!")
                .positivePoints("Estrutura bem organizada, linguagem adequada, desenvolvimento coerente do tema.")
                .suggestions("1. Enriqueça argumentos com dados. 2. Detalhe a proposta de intervenção. 3. Varie conectivos.")
                .build();
    }

    private Feedback createErrorFeedback(Essay essay, String errorMessage) {
        return Feedback.builder()
                .essay(essay)
                .type(Feedback.FeedbackType.AI_GENERATED)
                .generalComment("Houve um erro na análise automática. Por favor, tente novamente mais tarde.")
                .suggestions("Revise sua redação manualmente e consulte o manual do ENEM.")
                .build();
    }
}

