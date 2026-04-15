package com.fiap.mecanica.shared.contract;

import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.core.usecase.AuthenticateUserUseCase;
import com.fiap.mecanica.shared.seguranca.infra.controller.AuthenticateController;
import com.fiap.mecanica.shared.seguranca.infra.controller.dto.LoginInputDto;
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
@WebMvcTest(controllers = AuthenticateController.class)
class AuthenticateControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @ParameterizedTest
    @CsvSource({
            "'{\"email\":\"\",\"password\":\"123456\"}', email, 'O email deve ser preenchido'",
            "'{\"email\":\"email@email.com\",\"password\":\"\"}', password, 'A senha deve ser preenchida'"
    })
    void shouldReturn400WithValidationMessage(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(authenticateUserUseCase);
    }

    @Test
    void shouldReturn200WhenParamsValid() throws Exception {
        String json = """
                {
                  "email": "email_3819b21f83a4",
                  "password": "password_72e5137522d8"
                }
                """;

        String token = "any-token";

        Mockito.when(authenticateUserUseCase.authenticate(Mockito.any(LoginInputDto.class))).thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.token").value(token));

        Mockito.verify(authenticateUserUseCase).authenticate(Mockito.any(LoginInputDto.class));
    }
}
