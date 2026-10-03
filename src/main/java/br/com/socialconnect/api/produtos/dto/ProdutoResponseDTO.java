package br.com.socialconnect.api.produtos.dto;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representação detalhada de um produto no estoque")
public record ProdutoResponseDTO(

        @Schema(description = "Identificador único do produto", example = "1")
        Long idProduto,

        @Schema(description = "Nome do produto", example = "Arroz 5kg")
        String nome,

        @Schema(description = "Categoria do produto", example = "ALIMENTO")
        CategoriaProduto categoria,

        @Schema(description = "Quantidade atual em estoque", example = "3")
        Integer estoqueAtual,

        @Schema(description = "Quantidade mínima de segurança", example = "10")
        Integer estoqueMinimo,

        @Schema(description = "Unidade de medida", example = "unidade")
        String unidadeMedida,

        @Schema(description = "Data de cadastro do produto", example = "2026-09-18")
        LocalDate dataCadastro,

        @Schema(description = "Indica se o estoque atual está abaixo do estoque mínimo", example = "true")
        boolean estoqueBaixo
) {

        public static ProdutoResponseDTO fromEntity(Produto produto) {
                return new ProdutoResponseDTO(
                        produto.getIdProduto(),
                        produto.getNome(),
                        produto.getCategoria(),
                        produto.getEstoqueAtual(),
                        produto.getEstoqueMinimo(),
                        produto.getUnidadeMedida(),
                        produto.getDataCadastro(),
                        produto.getEstoqueAtual() < produto.getEstoqueMinimo() // Regra: true se estoqueAtual < estoqueMinimo
                );
        }
}