package com.fiap.mecanica.shared.seguranca.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
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

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = AuthenticateHttpController.class)
class AuthenticateControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticacaoGateway autenticacaoGateway;

    @MockitoBean
    private TokenGateway tokenGateway;

    @MockitoBean
    private ClienteGateway clienteGateway;

    @MockitoBean
    private UserGateway userGateway;

    @ParameterizedTest
    @CsvSource({
            "'{\"cpf\":\"\",\"password\":\"123456\"}', cpf, 'O CPF deve ser preenchido'",
            "'{\"cpf\":\"52998224725\",\"password\":\"\"}', password, 'A senha deve ser preenchida'"
    })
    void shouldReturn400WithValidationMessage(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(autenticacaoGateway, tokenGateway);
    }

    @Test
    void shouldReturn200WhenParamsValid() throws Exception {
        String json = """
                {
                  "cpf": "52998224725",
                  "password": "password123"
                }
                """;

        var user = new User(1L, new Cpf("52998224725"), new PasswordHash("hash"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE)));
        String token = "Bearer any-token";

        Mockito.when(autenticacaoGateway.autenticar(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(user);
        Mockito.when(tokenGateway.generateToken(Mockito.any(User.class)))
                .thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.token").value(token));

        Mockito.verify(autenticacaoGateway).autenticar(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(tokenGateway).generateToken(Mockito.any(User.class));
    }

    @Test
    void shouldReturn200WithUserIdWhenClienteExistsAndUserAlreadyExists() throws Exception {
        String cpf = "52998224725";
        Mockito.when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.of(Cliente.reconstituir(1L, "Pedro", null, cpf)));
        var user = new User(10L, new Cpf(cpf), new PasswordHash("hash"), List.of(new Role(1L, RoleEnum.ROLE_CLIENTE)));
        Mockito.when(userGateway.findByCpf(cpf)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate/cliente/status").param("cpf", cpf))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.userId").value(10));

        Mockito.verify(userGateway, Mockito.never()).create(Mockito.any());
    }

    @Test
    void shouldReturn404WhenClienteDoesNotExist() throws Exception {
        String cpf = "52998224725";
        Mockito.when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate/cliente/status").param("cpf", cpf))
                .andExpect(status().isNotFound());

        Mockito.verifyNoInteractions(userGateway);
    }
}
