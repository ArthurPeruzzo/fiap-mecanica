package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularPecaNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.MecanicoNaoResponsavelPelaOrdemDeServicoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoAbertaParaVeiculoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoSemServicosException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoMecanicoResponsavelException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.PecaNaoVinculadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.QuantidadeDesvincularInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoJaVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VeiculoNaoPertenceAoClienteException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoPecaNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoServicoNaoAutorizadoException;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = OrdemDeServicoController.class)
class OrdemDeServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase;

    @MockitoBean
    private IniciarDiagnosticoOrdemDeServicoUseCase iniciarDiagnosticoOrdemDeServicoUseCase;

    @MockitoBean
    private ConcluirDiagnosticoOrdemDeServicoUseCase concluirDiagnosticoOrdemDeServicoUseCase;

    @MockitoBean
    private VincularServicoOrdemDeServicoUseCase vincularServicoOrdemDeServicoUseCase;

    @MockitoBean
    private DesvincularServicoOrdemDeServicoUseCase desvincularServicoOrdemDeServicoUseCase;

    @MockitoBean
    private VincularPecaOrdemDeServicoUseCase vincularPecaOrdemDeServicoUseCase;

    @MockitoBean
    private DesvincularPecaOrdemDeServicoUseCase desvincularPecaOrdemDeServicoUseCase;

    @MockitoBean
    private VincularInsumoOrdemDeServicoUseCase vincularInsumoOrdemDeServicoUseCase;

    @MockitoBean
    private DesvincularInsumoOrdemDeServicoUseCase desvincularInsumoOrdemDeServicoUseCase;

    @MockitoBean
    private EnviarOrcamentoOrdemDeServicoUseCase enviarOrcamentoOrdemDeServicoUseCase;

    private static final String VALID_BODY = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"Barulho ao frear\"}";

    @Test
    void shouldReturn201WhenValidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(criarOrdemDeServicoUseCase).criar(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"veiculoId\":2,\"descricao\":\"Barulho ao frear\"}', clienteId, 'O cliente deve ser informado'",
            "'{\"clienteId\":1,\"descricao\":\"Barulho ao frear\"}', veiculoId, 'O veículo deve ser informado'",
            "'{\"clienteId\":1,\"veiculoId\":2}', descricao, 'A descrição deve ser informada'"
    })
    void shouldReturn400WhenRequiredFieldIsMissing(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarOrdemDeServicoUseCase);
    }

    @Test
    void shouldReturn404WhenAtendenteNotFound() throws Exception {
        Mockito.doThrow(new AtendenteNaoEncontradoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Atendente não encontrado"));
    }

    @Test
    void shouldReturn404WhenClienteNotFound() throws Exception {
        Mockito.doThrow(new ClienteNaoEncontradoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturn404WhenVeiculoNotFound() throws Exception {
        Mockito.doThrow(new VeiculoNaoEncontradoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn422WhenVeiculoNaoPertenceAoCliente() throws Exception {
        Mockito.doThrow(new VeiculoNaoPertenceAoClienteException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Veículo não pertence ao cliente informado"));
    }

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() throws Exception {
        Mockito.doThrow(new OrdemDeServicoAbertaParaVeiculoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Já existe uma ordem de serviço aberta para este veículo"));
    }

    // --- iniciarDiagnostico ---

    @Test
    void shouldReturn204WhenIniciarDiagnosticoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNoContent());

        Mockito.verify(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnIniciar() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(99L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnIniciar() throws Exception {
        Mockito.doThrow(new MecanicoNaoEncontradoException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenOrdemJaEmDiagnostico() throws Exception {
        Mockito.doThrow(new TransicaoDeStatusInvalidaException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    @Test
    void shouldReturn422WhenOutroMecanicoJaVinculado() throws Exception {
        Mockito.doThrow(new OrdemDeServicoMecanicoResponsavelException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Já existe um mecanico responsavel pela ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnIniciar() throws Exception {
        Mockito.doThrow(new TransicaoDeStatusInvalidaException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // --- concluirDiagnostico ---

    @Test
    void shouldReturn204WhenConcluirDiagnosticoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNoContent());

        Mockito.verify(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnConcluir() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(99L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnConcluir() throws Exception {
        Mockito.doThrow(new MecanicoNaoEncontradoException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenMecanicoNaoEhResponsavelOnConcluir() throws Exception {
        Mockito.doThrow(new MecanicoNaoResponsavelPelaOrdemDeServicoException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Somente o mecânico responsável pelo diagnóstico pode concluí-lo"));
    }

    @Test
    void shouldReturn422WhenSemServicosVinculadosOnConcluir() throws Exception {
        Mockito.doThrow(new OrdemDeServicoSemServicosException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível concluir o diagnóstico sem ao menos um serviço vinculado"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnConcluir() throws Exception {
        Mockito.doThrow(new TransicaoDeStatusInvalidaException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // --- vincularServico ---

    @Test
    void shouldReturn204WhenVincularServicoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isNoContent());

        Mockito.verify(vincularServicoOrdemDeServicoUseCase).vincular(1L, 10L);
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincular() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(vincularServicoOrdemDeServicoUseCase).vincular(99L, 10L);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/servicos/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnVincular() throws Exception {
        Mockito.doThrow(new ServicoNaoEncontradoException())
                .when(vincularServicoOrdemDeServicoUseCase).vincular(1L, 99L);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoJaVinculado() throws Exception {
        Mockito.doThrow(new ServicoJaVinculadoException())
                .when(vincularServicoOrdemDeServicoUseCase).vincular(1L, 10L);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Este serviço já está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincular() throws Exception {
        Mockito.doThrow(new VinculoServicoNaoAutorizadoException())
                .when(vincularServicoOrdemDeServicoUseCase).vincular(1L, 10L);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico"));
    }

    // --- desvincularServico ---

    @Test
    void shouldReturn204WhenDesvincularServicoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isNoContent());

        Mockito.verify(desvincularServicoOrdemDeServicoUseCase).desvincular(1L, 10L);
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincular() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(desvincularServicoOrdemDeServicoUseCase).desvincular(99L, 10L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/servicos/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDesvincular() throws Exception {
        Mockito.doThrow(new ServicoNaoEncontradoException())
                .when(desvincularServicoOrdemDeServicoUseCase).desvincular(1L, 99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculado() throws Exception {
        Mockito.doThrow(new ServicoNaoVinculadoException())
                .when(desvincularServicoOrdemDeServicoUseCase).desvincular(1L, 10L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincular() throws Exception {
        Mockito.doThrow(new VinculoServicoNaoAutorizadoException())
                .when(desvincularServicoOrdemDeServicoUseCase).desvincular(1L, 10L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico"));
    }

    // --- vincularPeca ---

    private static final String VALID_PECA_BODY = "{\"quantidade\":2}";

    @Test
    void shouldReturn204WhenVincularPecaSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(vincularPecaOrdemDeServicoUseCase).vincular(1L, 5L, 2);
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenVincularPecaRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(vincularPecaOrdemDeServicoUseCase);
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularPeca() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(vincularPecaOrdemDeServicoUseCase).vincular(99L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnVincularPeca() throws Exception {
        Mockito.doThrow(new PecaNaoEncontradaException())
                .when(vincularPecaOrdemDeServicoUseCase).vincular(1L, 99L, 2);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularPeca() throws Exception {
        Mockito.doThrow(new EstoqueInsuficienteException())
                .when(vincularPecaOrdemDeServicoUseCase).vincular(1L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincularPeca() throws Exception {
        Mockito.doThrow(new VinculoPecaNaoAutorizadaException())
                .when(vincularPecaOrdemDeServicoUseCase).vincular(1L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível vincular peças se a ordem de serviço não está em diagnóstico"));
    }

    // --- desvincularPeca ---

    @Test
    void shouldReturn204WhenDesvincularPecaSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(desvincularPecaOrdemDeServicoUseCase).desvincular(1L, 5L, 2);
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenDesvincularPecaRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(desvincularPecaOrdemDeServicoUseCase);
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularPeca() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(desvincularPecaOrdemDeServicoUseCase).desvincular(99L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDesvincularPeca() throws Exception {
        Mockito.doThrow(new PecaNaoEncontradaException())
                .when(desvincularPecaOrdemDeServicoUseCase).desvincular(1L, 99L, 2);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincularPeca() throws Exception {
        Mockito.doThrow(new DesvincularPecaNaoAutorizadaException())
                .when(desvincularPecaOrdemDeServicoUseCase).desvincular(1L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível desvincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenPecaNaoVinculadaOnDesvincularPeca() throws Exception {
        Mockito.doThrow(new PecaNaoVinculadaException())
                .when(desvincularPecaOrdemDeServicoUseCase).desvincular(1L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Peça não está vinculada à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeDesvincularInvalidaOnDesvincularPeca() throws Exception {
        Mockito.doThrow(new QuantidadeDesvincularInvalidaException())
                .when(desvincularPecaOrdemDeServicoUseCase).desvincular(1L, 5L, 2);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    // --- vincularInsumo ---

    private static final String VALID_INSUMO_BODY = "{\"quantidade\":3}";

    @Test
    void shouldReturn204WhenVincularInsumoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(vincularInsumoOrdemDeServicoUseCase).vincular(1L, 30L, 3);
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenVincularInsumoRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(vincularInsumoOrdemDeServicoUseCase);
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularInsumo() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(vincularInsumoOrdemDeServicoUseCase).vincular(99L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnVincularInsumo() throws Exception {
        Mockito.doThrow(new InsumoNaoEncontradoException())
                .when(vincularInsumoOrdemDeServicoUseCase).vincular(1L, 99L, 3);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularInsumo() throws Exception {
        Mockito.doThrow(new EstoqueInsuficienteException())
                .when(vincularInsumoOrdemDeServicoUseCase).vincular(1L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincularInsumo() throws Exception {
        Mockito.doThrow(new VinculoInsumoNaoAutorizadaException())
                .when(vincularInsumoOrdemDeServicoUseCase).vincular(1L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível vincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    // --- desvincularInsumo ---

    @Test
    void shouldReturn204WhenDesvincularInsumoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(desvincularInsumoOrdemDeServicoUseCase).desvincular(1L, 30L, 3);
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenDesvincularInsumoRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(desvincularInsumoOrdemDeServicoUseCase);
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularInsumo() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(desvincularInsumoOrdemDeServicoUseCase).desvincular(99L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDesvincularInsumo() throws Exception {
        Mockito.doThrow(new InsumoNaoEncontradoException())
                .when(desvincularInsumoOrdemDeServicoUseCase).desvincular(1L, 99L, 3);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincularInsumo() throws Exception {
        Mockito.doThrow(new DesvincularInsumoNaoAutorizadaException())
                .when(desvincularInsumoOrdemDeServicoUseCase).desvincular(1L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenInsumoNaoVinculadoOnDesvincularInsumo() throws Exception {
        Mockito.doThrow(new InsumoNaoVinculadoException())
                .when(desvincularInsumoOrdemDeServicoUseCase).desvincular(1L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insumo não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeDesvincularInvalidaOnDesvincularInsumo() throws Exception {
        Mockito.doThrow(new QuantidadeDesvincularInvalidaException())
                .when(desvincularInsumoOrdemDeServicoUseCase).desvincular(1L, 30L, 3);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Quantidade a desvincular é maior que a quantidade vinculada"));
    }
}
