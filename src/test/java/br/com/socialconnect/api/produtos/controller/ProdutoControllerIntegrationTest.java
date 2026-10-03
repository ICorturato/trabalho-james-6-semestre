package br.com.socialconnect.api.produtos.controller;

import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Testcontainers
class ProdutoControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar produto com sucesso e retornar 201 com Location")
    void deveCriarProdutoQuandoDadosValidos() throws Exception {
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Leite Integral 1L",
                CategoriaProduto.ALIMENTO,
                50,
                20,
                "unidade"
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/v1/produtos/")))
                .andExpect(jsonPath("$.idProduto", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Leite Integral 1L")))
                .andExpect(jsonPath("$.categoria", is("ALIMENTO")))
                .andExpect(jsonPath("$.estoqueBaixo", is(false)));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict quando tentar cadastrar produto com nome duplicado")
    void deveRetornar409QuandoNomeDuplicado() throws Exception {
        ProdutoRequestDTO dto1 = new ProdutoRequestDTO(
                "Sabonete Neutro",
                CategoriaProduto.HIGIENE,
                30,
                10,
                "unidade"
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Nome de produto já cadastrado")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Deve retornar 422 Unprocessable Entity quando o estoque for negativo")
    void deveRetornar422QuandoEstoqueNegativo() throws Exception {
        String jsonInvalido = """
            {
                "nome": "Cobertor de Casal",
                "categoria": "ROUPA",
                "estoqueAtual": -3,
                "estoqueMinimo": 5,
                "unidadeMedida": "unidade"
            }
        """;

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.title", is("Operação de estoque inválida")));
    }
}