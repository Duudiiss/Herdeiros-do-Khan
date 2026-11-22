package br.herdeiros.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/** Garante que os novos saves TXT preservam o estado do jogo e das cartas. */
public class SalvamentoCompletoTxtTest {

    @Test
    public void preservaConselheirosCartaSecretaEBaralhos() throws IOException {
        InterfacePartida original = new InterfacePartida(4);
        assertTrue(original.escolherConselheiroSecretoJogadorAtual(0));
        assertTrue(original.registrarVotosConquistados(2));

        Path primeiro = Files.createTempFile("herdeiros-original-", ".txt");
        Path segundo = Files.createTempFile("herdeiros-recarregado-", ".txt");
        try {
            GerenciadorSave.salvar(primeiro, original);
            InterfacePartida carregada = GerenciadorSave.carregar(primeiro);
            assertEquals(4, carregada.getQuantidadeJogadores());
            assertEquals(2, carregada.consultarVotosJogador(0));
            assertEquals(original.consultarConselheiros().size(),
                    carregada.consultarConselheiros().size());
            assertTrue(carregada.consultarObjetivosJogadorAtual().getOpcoesSecretasIniciais().isEmpty());
            assertEquals(4, carregada.consultarResultadoFinal().size());

            GerenciadorSave.salvar(segundo, carregada);
            JsonElement antes = JsonParser.parseString(Files.readString(primeiro, StandardCharsets.US_ASCII));
            JsonElement depois = JsonParser.parseString(Files.readString(segundo, StandardCharsets.US_ASCII));
            assertEquals(antes, depois);
        } finally {
            Files.deleteIfExists(primeiro);
            Files.deleteIfExists(segundo);
        }
    }

    @Test
    public void gravaAsciiERecuperaNomeComAcento() throws IOException {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"João", "Érica"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO});
        Path arquivo = Files.createTempFile("herdeiros-ascii-", ".txt");
        try {
            GerenciadorSave.salvar(arquivo, partida);
            byte[] bytes = Files.readAllBytes(arquivo);
            for (byte b : bytes) {
                assertTrue("Arquivo não está em ASCII puro", (b & 0xFF) < 128);
            }
            InterfacePartida carregada = GerenciadorSave.carregar(arquivo);
            assertEquals("João", carregada.getNomeJogador(0));
            assertEquals("Érica", carregada.getNomeJogador(1));
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoPermiteSalvarDepoisDeAtivarColuna() throws IOException {
        InterfacePartida partida = new InterfacePartida();
        assertTrue(partida.podeSalvarEntreTurnos());
        assertTrue(partida.ativarColunaJogadorAtual(0));
        assertFalse(partida.podeSalvarEntreTurnos());
        Path arquivo = Files.createTempFile("herdeiros-bloqueado-", ".txt");
        try {
            GerenciadorSave.salvar(arquivo, partida);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test
    public void nomeadoUsaSomenteExtensaoTxt() {
        assertEquals("partida.txt", GerenciadorSave.getCaminhoSaveLocal().getFileName().toString());
        assertEquals("rodada.txt", GerenciadorSave.getCaminhoSave("rodada").getFileName().toString());
        assertFalse(GerenciadorSave.getCaminhoSave("rodada").toString().endsWith(".json"));
    }
}
