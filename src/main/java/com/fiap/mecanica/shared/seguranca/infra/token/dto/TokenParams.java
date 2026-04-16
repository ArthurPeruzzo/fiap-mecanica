package com.fiap.mecanica.shared.seguranca.infra.token.dto;

import java.util.List;

public record TokenParams(Long userId, String email, List<String> roles) {
}
