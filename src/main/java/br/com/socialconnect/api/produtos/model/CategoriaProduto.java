package br.com.socialconnect.api.produtos.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo ou categoria do produto")
public enum CategoriaProduto {
    ALIMENTO,
    ROUPA,
    HIGIENE,
    OUTROS
}