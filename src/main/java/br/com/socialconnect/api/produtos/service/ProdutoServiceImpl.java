package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.NomeProdutoDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoServiceImpl(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ProdutoResponseDTO> listar(String nome, CategoriaProduto categoria, Pageable pageable) {
        Page<Produto> page;

        if (nome != null && !nome.isBlank() && categoria != null) {
            page = repository.findByNomeContainingIgnoreCaseAndCategoria(nome, categoria, pageable);
        } else if (nome != null && !nome.isBlank()) {
            page = repository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (categoria != null) {
            page = repository.findByCategoria(categoria, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(this::toResponseDTO);
    }

    @Override
    public ProdutoResponseDTO buscarPorId(Long idProduto) {
        return repository.findById(idProduto)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado com o ID: " + idProduto));
    }

    @Override
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        if (dto.estoqueAtual() != null && dto.estoqueAtual() < 0) {
            throw new EstoqueNegativoException("O estoque atual não pode ser negativo.");
        }
        if (dto.estoqueMinimo() != null && dto.estoqueMinimo() < 0) {
            throw new EstoqueNegativoException("O estoque mínimo não pode ser negativo.");
        }

        if (repository.existsByNomeIgnoreCase(dto.nome())) {
            throw new NomeProdutoDuplicadoException(dto.nome());
        }

        Produto entity = Produto.builder()
                .nome(dto.nome())
                .categoria(dto.categoria())
                .estoqueAtual(dto.estoqueAtual())
                .estoqueMinimo(dto.estoqueMinimo())
                .unidadeMedida(dto.unidadeMedida())
                .dataCadastro(LocalDate.now())
                .build();

        return toResponseDTO(repository.save(entity));
    }

    @Override
    public ProdutoResponseDTO atualizar(Long idProduto, ProdutoRequestDTO dto) {
        Produto entity = repository.findById(idProduto)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado com o ID: " + idProduto));

        if (dto.estoqueAtual() != null && dto.estoqueAtual() < 0) {
            throw new EstoqueNegativoException("O estoque atual não pode ser negativo.");
        }
        if (dto.estoqueMinimo() != null && dto.estoqueMinimo() < 0) {
            throw new EstoqueNegativoException("O estoque mínimo não pode ser negativo.");
        }

        if (repository.existsByNomeIgnoreCaseAndIdProdutoNot(dto.nome(), idProduto)) {
            throw new NomeProdutoDuplicadoException(dto.nome());
        }

        entity.setNome(dto.nome());
        entity.setCategoria(dto.categoria());
        entity.setEstoqueAtual(dto.estoqueAtual());
        entity.setEstoqueMinimo(dto.estoqueMinimo());
        entity.setUnidadeMedida(dto.unidadeMedida());

        return toResponseDTO(repository.save(entity));
    }

    @Override
    public void deletar(Long idProduto) {
        if (!repository.existsById(idProduto)) {
            throw new RecursoNaoEncontradoException(
                    "Produto não encontrado com o ID: " + idProduto);
        }
        repository.deleteById(idProduto);
    }

    private ProdutoResponseDTO toResponseDTO(Produto entity) {
        return new ProdutoResponseDTO(
                entity.getIdProduto(),
                entity.getNome(),
                entity.getCategoria(),
                entity.getEstoqueAtual(),
                entity.getEstoqueMinimo(),
                entity.getUnidadeMedida(),
                entity.getDataCadastro(),
                entity.getEstoqueAtual() < entity.getEstoqueMinimo()
        );
    }
}