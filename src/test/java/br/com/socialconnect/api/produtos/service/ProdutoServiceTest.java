package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.NomeProdutoDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoServiceImpl service;

    @Test
    @DisplayName("Deve criar produto com sucesso quando os dados forem válidos")
    void deveCriarProdutoQuandoDadosValidos() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg",
                CategoriaProduto.ALIMENTO,
                15,
                10,
                "unidade"
        );

        Produto produtoSalvo = Produto.builder()
                .idProduto(1L)
                .nome("Arroz 5kg")
                .categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(15)
                .estoqueMinimo(10)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.now())
                .build();

        when(repository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(false);
        when(repository.save(any(Produto.class))).thenReturn(produtoSalvo);

        ProdutoResponseDTO resultado = service.criar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.idProduto());
        assertEquals("Arroz 5kg", resultado.nome());
        assertEquals(CategoriaProduto.ALIMENTO, resultado.categoria());
        assertEquals(15, resultado.estoqueAtual());
        assertEquals(10, resultado.estoqueMinimo());
        assertFalse(resultado.estoqueBaixo());
        verify(repository, times(1)).existsByNomeIgnoreCase("Arroz 5kg");
        verify(repository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o estoque atual for negativo")
    void deveLancarExcecaoQuandoEstoqueNegativo() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Feijão 1kg",
                CategoriaProduto.ALIMENTO,
                -5,
                10,
                "unidade"
        );

        EstoqueNegativoException exception = assertThrows(
                EstoqueNegativoException.class,
                () -> service.criar(dto)
        );

        assertEquals("O estoque atual não pode ser negativo.", exception.getMessage());
        verify(repository, never()).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome do produto for duplicado")
    void deveLancarExcecaoQuandoNomeDuplicado() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg",
                CategoriaProduto.ALIMENTO,
                20,
                10,
                "unidade"
        );

        when(repository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(true);

        assertThrows(
                NomeProdutoDuplicadoException.class,
                () -> service.criar(dto)
        );

        verify(repository, times(1)).existsByNomeIgnoreCase("Arroz 5kg");
        verify(repository, never()).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve calcular alerta de estoque baixo como true quando estoque atual for menor que o mínimo")
    void deveAlertarEstoqueBaixoQuandoAbaixoDoMinimo() {
        Produto produto = Produto.builder()
                .idProduto(2L)
                .nome("Óleo de Soja 900ml")
                .categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(3)
                .estoqueMinimo(10)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.now())
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(produto));

        ProdutoResponseDTO resultado = service.buscarPorId(2L);

        assertNotNull(resultado);
        assertTrue(resultado.estoqueBaixo());
        verify(repository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando buscar por ID inexistente")
    void deveLancarExcecaoQuandoBuscarPorIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarPorId(99L)
        );
        verify(repository, times(1)).findById(99L);
    }
}