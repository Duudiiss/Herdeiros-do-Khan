package br.herdeiros.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import br.herdeiros.mapa.MapaDados;
import org.junit.Test;

public class GerenciadorSaveTest {

    @Test
    public void salvaECarregaUmSaveNomeadoNaPastaDeSaves() throws IOException {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"A", "B"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO});
        Path arquivo = GerenciadorSave.getCaminhoSave("teste-fluxo");

        try {
            GerenciadorSave.salvarNomeado("teste-fluxo", partida);
            String conteudo = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
            assertTrue(conteudo.contains("\"formato\": \"herdeiros-save\""));
            assertTrue(conteudo.contains("\"jogadores\""));
            assertTrue(!conteudo.contains("\"payload\""));

            conteudo = conteudo.replace("\"nome\": \"A\"", "\"nome\": \"Ana\"");
            Files.write(arquivo, conteudo.getBytes(StandardCharsets.UTF_8));
            InterfacePartida carregada = GerenciadorSave.carregarNomeado("teste-fluxo");

            assertEquals("teste-fluxo.txt", arquivo.getFileName().toString());
            assertEquals("Ana", carregada.getNomeJogador(0));
            assertTrue(GerenciadorSave.listarSaves().contains("teste-fluxo.txt"));
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test
    public void salvaEstadoDaPartidaEPermiteContinuar() throws IOException {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"A", "B"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO});
        MapaDados mapa = new MapaDados();
        for (int id = 1; id <= 80; id++) {
            assertTrue(mapa.adicionar(id, 0.4, 0.4, false));
            assertTrue(mapa.conectar(id - 1, id));
        }
        assertTrue(partida.carregarMapa(mapa));
        assertTrue(partida.movimentarJogador(0, 1));
        assertTrue(partida.registrarVotosConquistados(3));
        assertTrue(partida.simularRecebimentoTributo(0, "ESPADA", 2));

        Path diretorio = Files.createTempDirectory("herdeiros-save-test");
        Path arquivo = diretorio.resolve("partida.txt");
        try {
            GerenciadorSave.salvar(arquivo, partida);
            assertTrue(partida.registrarVotosConquistados(2));
            GerenciadorSave.salvar(arquivo, partida);
            InterfacePartida carregada = GerenciadorSave.carregar(arquivo);

            assertEquals(1, carregada.getParadaJogador(0));
            assertEquals(5, carregada.consultarVotosJogador(0));
            assertEquals(2, carregada.consultarCartela(0).getEspadas());
            assertEquals(81, carregada.getMapaDadosSave().getPontos().size());
            assertTrue(carregada.movimentarJogador(0, 0));
            assertEquals(0, carregada.getParadaJogador(0));
        } finally {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(diretorio);
        }
    }

    @Test
    public void carregaSaveJsonLegadoComPayloadBase64() throws IOException {
        InterfacePartida partida = new InterfacePartida();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(partida);
        }
        String conteudo = "{\n"
                + "  \"formato\": \"herdeiros-save\",\n"
                + "  \"versao\": 1,\n"
                + "  \"payload\": \"" + Base64.getEncoder().encodeToString(bytes.toByteArray()) + "\"\n"
                + "}\n";
        Path arquivo = Files.createTempFile("herdeiros-save-legado", ".json");
        try {
            Files.write(arquivo, conteudo.getBytes(StandardCharsets.UTF_8));
            assertEquals(2, GerenciadorSave.carregar(arquivo).getQuantidadeJogadores());
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test(expected = IOException.class)
    public void rejeitaArquivoQueNaoContemUmSave() throws IOException {
        Path arquivo = Files.createTempFile("herdeiros-save-invalido", ".save");
        try {
            Files.write(arquivo, new byte[] {1, 2, 3});
            GerenciadorSave.carregar(arquivo);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
