package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
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
    @MockitoBean private MetricasGateway metricasGateway;
    @MockitoBean private UserGateway userGateway;

    private static final Long USER_ID = 10L;
    private static final Long ATENDENTE_ID = 3L;
    private static final Long CLIENTE_ID = 1L;
    private static final Long VEICULO_ID = 2L;
    private static final Long MECANICO_ID = 5L;

    private static final String VALID_BODY = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"Barulho ao frear\"}";

    // ---- domain helpers ----

    private Atendente atendente() {
        return Atendente.builder().id(ATENDENTE_ID).nomeCompleto(new NomeCompleto("João", "Silva")).build();
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

    private OrdemDeServico ordemFinalizada() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.FINALIZADA, List.of(), List.of(), List.of(), new Orcamento(BigDecimal.TEN));
    }

    // ---- criar ----

    @Test
    void shouldReturn201WhenValidRequest() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(ordemDeServicoGateway).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WithOrdemIdInBody() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(42L);

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(42));
    }

    @Test
    void shouldReturn201WhenRequestIncludesPecasAndInsumos() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);
        Mockito.when(servicoGateway.listarPorIds(List.of(10L)))
                .thenReturn(List.of(Servico.reconstituir(10L, "Serviço", "desc", BigDecimal.TEN)));
        Mockito.when(pecaGateway.listarPorIds(List.of(5L)))
                .thenReturn(List.of(Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, 10)));
        Mockito.when(insumoGateway.listarPorIds(List.of(30L)))
                .thenReturn(List.of(Insumo.reconstituir(30L, "Insumo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 10)));

        String bodyWithLinks = "{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"Barulho ao frear\"," +
                "\"servicosIds\":[10],\"pecas\":[{\"id\":5,\"quantidade\":2}],\"insumos\":[{\"id\":30,\"quantidade\":1}]}";

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
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Atendente não encontrado"));
    }

    @Test
    void shouldReturn404WhenVeiculoNotFound() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn404WhenClienteNotFound() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnCriar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);
        Mockito.when(servicoGateway.listarPorIds(Mockito.anyList())).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"servicosIds\":[99]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnCriar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);
        Mockito.when(pecaGateway.listarPorIds(Mockito.anyList())).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"id\":99,\"quantidade\":1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnCriar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);
        Mockito.when(insumoGateway.listarPorIds(Mockito.anyList())).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"insumos\":[{\"id\":99,\"quantidade\":1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenVeiculoNaoPertenceAoCliente() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID))
                .thenReturn(Optional.of(Veiculo.reconstituir(VEICULO_ID, 99L, "ABC1234", "Gol", 2020)));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Veículo não pertence ao cliente informado"));
    }

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Já existe uma ordem de serviço aberta para este veículo"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnCriar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(1L);
        Mockito.when(pecaGateway.listarPorIds(Mockito.anyList()))
                .thenReturn(List.of(Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, 0)));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"descricao\":\"teste\",\"pecas\":[{\"id\":5,\"quantidade\":2}]}"))
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
        Mockito.when(ordemDeServicoGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(ordemRecebida()), 0, 10, 1L, 1));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(atendenteGateway.findById(ATENDENTE_ID)).thenReturn(Optional.of(atendente()));

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
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

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

    // ---- minhasOrdens ----

    @Test
    void shouldReturn200WithOnlyOrdensFromAuthenticatedClienteOnMinhasOrdens() throws Exception {
        var user = new User(USER_ID, new Cpf("12345678909"), new PasswordHash("hash"),
                List.of(new Role(1L, RoleEnum.ROLE_CLIENTE)));

        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(userGateway.findById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(clienteGateway.buscarPorCpf("12345678909")).thenReturn(Optional.of(cliente()));
        Mockito.when(ordemDeServicoGateway.listarPorClienteId(CLIENTE_ID, 0, 10))
                .thenReturn(new Pagina<>(List.of(ordemRecebida()), 0, 10, 1L, 1));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculo()));
        Mockito.when(atendenteGateway.findById(ATENDENTE_ID)).thenReturn(Optional.of(atendente()));

        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/minhas-ordens")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nomeCliente").value("Maria Santos"));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).listar(Mockito.anyInt(), Mockito.anyInt());
    }
}
