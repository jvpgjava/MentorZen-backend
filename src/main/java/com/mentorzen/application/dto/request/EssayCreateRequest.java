package com.mentorzen.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para criação ou atualização de uma redação")
public class EssayCreateRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    @Schema(description = "Título da redação", example = "A Importância da Educação Digital no Brasil", maxLength = 200)
    private String title;

    @NotBlank(message = "Tema é obrigatório")
    @Size(max = 500, message = "Tema deve ter no máximo 500 caracteres")
    @Schema(description = "Tema ou proposta da redação conforme ENEM",
            example = "Com base na leitura dos textos motivadores e nos conhecimentos construídos ao longo de sua formação, redija texto dissertativo-argumentativo em modalidade escrita formal da língua portuguesa sobre o tema 'A importância da educação digital no Brasil', apresentando proposta de intervenção que respeite os direitos humanos.",
            maxLength = 500)
    private String theme;

    @NotBlank(message = "Conteúdo é obrigatório")
    @Schema(description = "Conteúdo completo da redação dissertativo-argumentativa",
            example = "A educação digital no Brasil representa um dos maiores desafios contemporâneos...",
            minLength = 150, maxLength = 5000)
    private String content;
}

