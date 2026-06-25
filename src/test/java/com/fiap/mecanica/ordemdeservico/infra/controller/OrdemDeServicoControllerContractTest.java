package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = OrdemDeServicoHttpController.class)
class OrdemDeServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private OrdemDeServicoGateway ordemDeServicoGateway;
    @MockitoBean private AtendenteGateway atendenteGateway;
    @MockitoBean private TokenGateway tokenGateway;
    @MockitoBean private VeiculoGateway veiculoGateway;
    @MockitoBean private ClienteGateway clienteGateway;
    @MockitoBean private MecanicoGateway mecanicoGateway;
    @MockitoBean private ServicoGateway servicoGateway;
    @MockitoBean private PecaGateway pecaGateway;
    @MockitoBean private InsumoGateway insumoGateway;
    @MockitoBean private NotificacaoGateway notificacaoGateway;

    private static final Long USER_ID = 10L;
    private static final Long MECANICO_ID = 5L;
    private static final Long ATENDENTE_ID = 3L;
    private static final Long CLIENTE_ID = 1L;
    private static final Long VEICULO_ID = 2L;

    private static final String VALID_BODY = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"Barulho ao frear\"}";
    private static final String VALID_PECA_BODY = "{\"quantidade\":2}";
    private static final String VALID_INSUMO_BODY = "{\"quantidade\":3}";

    // ---- domain helpers ----

    private Atendente atendente() {
        return Atendente.builder().id(ATENDENTE_ID).nomeCompleto(new NomeCompleto("João", "Silva")).build();
    }

    private Mecanico mecanico() {
        return Mecanico.builder().id(MECANICO_ID).nomeCompleto(new NomeCompleto("Carlos", "Lima")).build();
    }

    private Cliente cliente() {
        return Cliente.reconstituir(CLIENTE_ID, "Maria Santos", null, "12345678909");
    }

    private Veiculo veiculo() {
        return Veiculo.reconstituir(VEICULO_ID, CLIENTE_ID, "ABC1234", "Civic", 2020);
    }

    private OrdemDeServico buildOrdem(Long mecanicoId, StatusOrdemDeServico status,
                                      List<ServicoVinculado> servicos,
                                      List<PecaVinculada> pecas,
                                      List<InsumoVinculado> insumos,
                                      Orcamento orcamento) {
        return OrdemDeServico.builder()
                .id(1L).clienteId(CLIENTE_ID).veiculoId(VEICULO_ID)
                .atendenteId(ATENDENTE_ID).mecanicoId(mecanicoId)
                .status(status).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(servicos))
                .pecasVinculadas(new ArrayList<>(pecas))
                .insumosVinculados(new ArrayList<>(insumos))
                .orcamento(orcamento)
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    private OrdemDeServico ordemRecebida() {
        return buildOrdem(null, StatusOrdemDeServico.RECEBIDA, List.of(), List.of(), List.of(), null);
    }

    private OrdemDeServico ordemComMecanicoJaVinculado() {
        return buildOrdem(99L, StatusOrdemDeServico.RECEBIDA, List.of(), List.of(), List.of(), null);
    }

    private OrdemDeServico ordemRecebidaComServico() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(null, StatusOrdemDeServico.RECEBIDA, List.of(sv), List.of(), List.of(), null);
    }

    private OrdemDeServico ordemEmDiagnostico() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(), List.of(), List.of(), null);
    }

    private OrdemDeServico ordemEmDiagnosticoComServico() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(sv), List.of(), List.of(), null);
    }

    private OrdemDeServico ordemEmDiagnosticoComServicoOutroMecanico() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(99L, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(sv), List.of(), List.of(), null);
    }

    private OrdemDeServico ordemEmDiagnosticoComPeca(Integer quantidade) {
        var pv = new PecaVinculada(5L, quantidade, BigDecimal.TEN);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(), List.of(pv), List.of(), null);
    }

    private OrdemDeServico ordemEmDiagnosticoComInsumo(Integer quantidade) {
        var iv = new InsumoVinculado(30L, quantidade, BigDecimal.TEN);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(), List.of(), List.of(iv), null);
    }

    private OrdemDeServico ordemDiagnosticoConcluido() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, List.of(sv), List.of(), List.of(), new Orcamento(BigDecimal.TEN));
    }

    private OrdemDeServico ordemEmExecucaoSemServicos() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of(), List.of(), List.of(), new Orcamento(BigDecimal.TEN));
    }

    private OrdemDeServico ordemEmExecucaoComServicoNaoIniciado() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of(sv), List.of(), List.of(), new Orcamento(BigDecimal.TEN));
    }

    private OrdemDeServico ordemEmExecucaoComServicoEmExecucao() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.EM_EXECUCAO, LocalDateTime.now(), null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of(sv), List.of(), List.of(), new Orcamento(BigDecimal.TEN));
    }

    private OrdemDeServico ordemFinalizada() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.FINALIZADA, List.of(), List.of(), List.of(), new Orcamento(BigDecimal.TEN));
    }

    // ---- setup ----

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.lenient().when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.lenient().when(atendenteGateway.findById(ATENDENTE_ID)).thenReturn(Optional.of(atendente()));
        Mockito.lenient().when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.lenient().when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.lenient().when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.lenient().when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.lenient().when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);
        Mockito.lenient().when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.lenient().when(servicoGateway.buscarPorId(10L))
                .thenReturn(Optional.of(Servico.reconstituir(10L, "Serviço", "desc", BigDecimal.TEN)));
        Mockito.lenient().when(pecaGateway.buscarPorId(5L))
                .thenReturn(Optional.of(Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, 10)));
        Mockito.lenient().when(insumoGateway.buscarPorId(30L))
                .thenReturn(Optional.of(Insumo.reconstituir(30L, "Insumo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 10)));
    }

    // ---- criar ----

    @Test
    void shouldReturn201WhenValidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(ordemDeServicoGateway).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WithOrdemIdInBody() throws Exception {
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(42L);

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(42));
    }

    @Test
    void shouldReturn201WhenRequestIncludesPecasAndInsumos() throws Exception {
        String bodyWithLinks = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"Barulho ao frear\"," +
                "\"servicosIds\":[10],\"pecas\":[{\"id\":5,\"quantidade\":2}],\"insumos\":[{\"id\":30,\"quantidade\":1}]}";
        Mockito.when(insumoGateway.listarPorIds(List.of(30L)))
                .thenReturn(List.of(Insumo.reconstituir(30L, "Insumo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 10)));
        Mockito.when(pecaGateway.listarPorIds(List.of(5L)))
                .thenReturn(List.of(Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, 10)));
        Mockito.when(servicoGateway.listarPorIds(List.of(10L)))
                .thenReturn(List.of(Servico.reconstituir(10L, "Serviço", "desc", BigDecimal.TEN)));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithLinks))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400WhenPecaIdIsNull() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"quantidade\":2}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPecaQuantidadeIsNullOrZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"id\":1}]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"id\":1,\"quantidade\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenInsumoIdIsNull() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"insumos\":[{\"quantidade\":2}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenInsumoQuantidadeIsNullOrZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"insumos\":[{\"id\":1}]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"insumos\":[{\"id\":1,\"quantidade\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenAtendenteNotFound() throws Exception {
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Atendente não encontrado"));
    }

    @Test
    void shouldReturn404WhenVeiculoNotFound() throws Exception {
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn404WhenClienteNotFound() throws Exception {
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnCriar() throws Exception {
        Mockito.when(servicoGateway.listarPorIds(Mockito.anyList())).thenReturn(List.of());
        String body = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"servicosIds\":[99]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnCriar() throws Exception {
        Mockito.when(pecaGateway.listarPorIds(Mockito.anyList())).thenReturn(List.of());
        String body = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"id\":99,\"quantidade\":1}]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnCriar() throws Exception {
        Mockito.when(insumoGateway.listarPorIds(Mockito.anyList())).thenReturn(List.of());
        String body = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"insumos\":[{\"id\":99,\"quantidade\":1}]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenVeiculoNaoPertenceAoCliente() throws Exception {
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID))
                .thenReturn(Optional.of(Veiculo.reconstituir(VEICULO_ID, 99L, "ABC1234", "Gol", 2020)));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Veículo não pertence ao cliente informado"));
    }

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() throws Exception {
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Já existe uma ordem de serviço aberta para este veículo"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnCriar() throws Exception {
        Mockito.when(pecaGateway.listarPorIds(Mockito.anyList()))
                .thenReturn(List.of(Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, 0)));
        String body = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"id\":5,\"quantidade\":2}]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
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
    }

    // ---- iniciarDiagnostico ----

    @Test
    void shouldReturn204WhenIniciarDiagnosticoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnIniciar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnIniciar() throws Exception {
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenOutroMecanicoJaVinculado() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemComMecanicoJaVinculado()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Já existe um mecanico responsavel pela ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnIniciar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComServico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // ---- concluirDiagnostico ----

    @Test
    void shouldReturn204WhenConcluirDiagnosticoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComServico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnConcluir() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnConcluir() throws Exception {
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenMecanicoNaoEhResponsavelOnConcluir() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L))
                .thenReturn(Optional.of(ordemEmDiagnosticoComServicoOutroMecanico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Somente o mecânico responsável pelo diagnóstico pode concluí-lo"));
    }

    @Test
    void shouldReturn422WhenSemServicosVinculadosOnConcluir() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnostico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível concluir o diagnóstico sem ao menos um serviço vinculado"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnConcluir() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // ---- vincularServico ----

    @Test
    void shouldReturn204WhenVincularServicoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincular() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/servicos/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnVincular() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoJaVinculado() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebidaComServico()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço já está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoERecebidaOnVincular() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- desvincularServico ----

    @Test
    void shouldReturn204WhenDesvincularServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebidaComServico()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincular() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/servicos/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDesvincular() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculado() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincular() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- vincularPeca ----

    @Test
    void shouldReturn204WhenVincularPecaSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNoContent());
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
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnVincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularPeca() throws Exception {
        Mockito.when(pecaGateway.buscarPorId(5L))
                .thenReturn(Optional.of(Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, 0)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível vincular peças se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- desvincularPeca ----

    @Test
    void shouldReturn204WhenDesvincularPecaSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComPeca(5)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNoContent());
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
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDesvincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível desvincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenPecaNaoVinculadaOnDesvincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnostico()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Peça não está vinculada à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeDesvincularInvalidaOnDesvincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComPeca(1)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    // ---- vincularInsumo ----

    @Test
    void shouldReturn204WhenVincularInsumoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNoContent());
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
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnVincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularInsumo() throws Exception {
        Mockito.when(insumoGateway.buscarPorId(30L))
                .thenReturn(Optional.of(Insumo.reconstituir(30L, "Insumo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 0)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível vincular insumos se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- desvincularInsumo ----

    @Test
    void shouldReturn204WhenDesvincularInsumoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComInsumo(5)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNoContent());
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
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDesvincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenInsumoNaoVinculadoOnDesvincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnostico()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Insumo não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeDesvincularInvalidaOnDesvincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComInsumo(1)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    // ---- iniciarServico ----

    @Test
    void shouldReturn204WhenIniciarServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoNaoIniciado()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnIniciarServico() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/servicos/10/iniciar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnIniciarServico() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/99/iniciar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculadoOnIniciarServico() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmExecucaoOnIniciarServico() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível iniciar um serviço se a ordem de serviço não está em execução"));
    }

    @Test
    void shouldReturn422WhenServicoJaIniciadoOnIniciarServico() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoEmExecucao()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço já foi iniciado ou finalizado"));
    }

    // ---- finalizarServico ----

    @Test
    void shouldReturn204WhenFinalizarServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoEmExecucao()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnFinalizar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/servicos/10/finalizar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnFinalizar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/99/finalizar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculadoOnFinalizar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmExecucaoOnFinalizar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível finalizar um serviço se a ordem de serviço não está em execução"));
    }

    @Test
    void shouldReturn422WhenServicoNaoIniciadoOuFinalizadoOnFinalizar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoNaoIniciado()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço ainda não foi iniciado ou já foi finalizado"));
    }

    // ---- entregar ----

    @Test
    void shouldReturn204WhenEntregarSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemFinalizada()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/entregar"))
                .andExpect(status().isNoContent());
    }

    // ---- detalhamento ----

    @Test
    void shouldReturn200WithPagedDetalhamentoWhenSuccess() throws Exception {
        var ordemPage = new Pagina<>(List.of(ordemRecebida()), 0, 10, 1L, 1);
        Mockito.when(ordemDeServicoGateway.listar(0, 10)).thenReturn(ordemPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/detalhamento")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nomeCliente").value("Maria Santos"))
                .andExpect(jsonPath("$.content[0].veiculo").value("Civic 2020 ABC-1234"))
                .andExpect(jsonPath("$.content[0].nomeAtendente").value("João Silva"))
                .andExpect(jsonPath("$.content[0].status").value("RECEBIDA"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    // ---- consultarStatus ----

    @Test
    void shouldReturn200WithStatusWhenConsultarStatusSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnConsultarStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/99/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }
}
