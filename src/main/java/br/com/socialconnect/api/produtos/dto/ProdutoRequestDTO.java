package br.com.socialconnect.api.produtos.dto;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.validation.EstoqueNaoNegativo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação ou atualização de um produto")
public record ProdutoRequestDTO(

        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        @Schema(description = "Nome do produto", example = "Arroz 5kg")
        String nome,

        @NotNull(message = "A categoria é obrigatória")
        @Schema(description = "Categoria do produto", example = "ALIMENTO")
        CategoriaProduto categoria,

        @NotNull(message = "O estoque atual é obrigatório")
        @EstoqueNaoNegativo(message = "O estoque atual não pode ser negativo")
        @Schema(description = "Quantidade atual em estoque", example = "3")
        Integer estoqueAtual,

        @NotNull(message = "O estoque mínimo é obrigatório")
        @EstoqueNaoNegativo(message = "O estoque mínimo não pode ser negativo")
        @Schema(description = "Quantidade mínima de segurança em estoque", example = "10")
        Integer estoqueMinimo,

        @NotBlank(message = "A unidade de medida é obrigatória")
        @Size(max = 20, message = "A unidade de medida deve ter no máximo 20 caracteres")
        @Schema(description = "Unidade de medida", example = "unidade")
        String unidadeMedida
) {}