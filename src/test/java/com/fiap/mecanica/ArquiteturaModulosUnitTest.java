package com.fiap.mecanica;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guarda a costura entre os módulos: o código de produção de ordemdeservico só pode
 * importar o módulo estoque dentro de infra/gateway/estoque (os adaptadores locais).
 * Ao extrair o estoque como microsserviço, apenas esses adaptadores são trocados.
 */
class ArquiteturaModulosUnitTest {

    private static final Path ORDEM_DE_SERVICO = Path.of("src/main/java/com/fiap/mecanica/ordemdeservico");
    private static final Path COSTURA = ORDEM_DE_SERVICO.resolve("infra/gateway/estoque");
    private static final String IMPORT_ESTOQUE = "import com.fiap.mecanica.estoque.";

    @Test
    void ordemDeServicoSoImportaEstoqueNosAdaptadoresDaCostura() throws IOException {
        List<String> violacoes;
        try (Stream<Path> arquivos = Files.walk(ORDEM_DE_SERVICO)) {
            violacoes = arquivos
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.startsWith(COSTURA))
                    .filter(this::importaEstoque)
                    .map(Path::toString)
                    .toList();
        }

        assertTrue(violacoes.isEmpty(), "Classes de ordemdeservico importando estoque fora da costura: " + violacoes);
    }

    private boolean importaEstoque(Path arquivo) {
        try {
            return Files.readAllLines(arquivo).stream().anyMatch(l -> l.startsWith(IMPORT_ESTOQUE));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
