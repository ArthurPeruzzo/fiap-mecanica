package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.ServicoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServicoDatabaseGateway implements ServicoGateway {

    private final ServicoRepository servicoRepository;

    @Override
    public void criar(Servico servico) {
        try {
            servicoRepository.save(ServicoEntity.builder()
                    .nome(servico.getNome())
                    .descricao(servico.getDescricao())
                    .preco(servico.getPreco())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao criar servico", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        try {
            return servicoRepository.findById(id)
                    .map(e -> Servico.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco()));
        } catch (Exception e) {
            log.error("Erro ao buscar servico por id: {}", id, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void atualizar(Servico servico) {
        try {
            servicoRepository.save(ServicoEntity.builder()
                    .id(servico.getId())
                    .nome(servico.getNome())
                    .descricao(servico.getDescricao())
                    .preco(servico.getPreco())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao atualizar servico", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void deletar(Long id) {
        try {
            servicoRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Erro ao deletar servico por id: {}", id, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public Pagina<Servico> listar(int page, int size) {
        try {
            var resultado = servicoRepository.findAll(PageRequest.of(page, size));
            var servicos = resultado.getContent().stream()
                    .map(e -> Servico.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco()))
                    .toList();
            return new Pagina<>(servicos, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
        } catch (Exception e) {
            log.error("Erro ao listar servicos", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public List<Servico> listarPorIds(List<Long> servicosIds) {
        try {
            return servicoRepository.findAllById(servicosIds)
                    .stream()
                    .map(e -> Servico.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco()))
                    .toList();
        } catch (Exception e) {
            log.error("Erro ao listar servicos por ids: {}", servicosIds, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
