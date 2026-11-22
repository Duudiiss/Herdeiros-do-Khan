package br.herdeiros.model;

import br.herdeiros.layout.LayoutAreas;
import br.herdeiros.layout.LayoutRaios;
import br.herdeiros.layout.LayoutMarcadores;
import br.herdeiros.mapa.IconesTabuleiro;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/** Verifica a persistencia das novas areas editaveis do unico editor. */
public class LayoutEditorCompletoTest {
    @Test
    public void raioDoPlacarEdoKhanSobrevivemAoArquivo() throws Exception {
        Path arquivo = Files.createTempFile("khan-raios-", ".csv");
        try {
            LayoutRaios dados = new LayoutRaios(arquivo);
            dados.definir("pontos.9", 0.017);
            dados.definir("khan.RUSSIA_OESTE", 0.043);
            dados.salvar();
            LayoutRaios restaurados = new LayoutRaios(arquivo);
            assertEquals(0.017, restaurados.consultar("pontos.9", 0.02), 0.0000001);
            assertEquals(0.043, restaurados.consultar("khan.RUSSIA_OESTE", 0.02), 0.0000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test
    public void contornoDaAtivacaoEIndependenteDasMelhorias() throws Exception {
        Path arquivo = Files.createTempFile("khan-areas-", ".csv");
        try {
            LayoutAreas dados = new LayoutAreas(arquivo);
            dados.marcar("ativacao.2", new LayoutAreas.Area(0.6, 0.8, 0.12, 0.14));
            dados.marcar("melhoria.2.0", new LayoutAreas.Area(0.6, 0.6, 0.16, 0.08));
            dados.salvar();
            LayoutAreas restaurados = new LayoutAreas(arquivo);
            assertEquals(0.14, restaurados.consultar("ativacao.2").altura, 0.0000001);
            assertEquals(0.08, restaurados.consultar("melhoria.2.0").altura, 0.0000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test
    public void posicaoDoKhanEGravadaSeparadamenteDosPontos() throws Exception {
        Path arquivo = Files.createTempFile("khan-centros-", ".csv");
        try {
            LayoutMarcadores dados = new LayoutMarcadores(arquivo);
            dados.marcar("khan.CHINA_LESTE", 0.7, 0.6);
            dados.marcar("pontos.12", 0.8, 0.9);
            dados.salvar();
            LayoutMarcadores restaurados = new LayoutMarcadores(arquivo);
            assertEquals(0.6, restaurados.consultar("khan.CHINA_LESTE").getY(), 0.0000001);
            assertEquals(0.8, restaurados.consultar("pontos.12").getX(), 0.0000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test
    public void posicoesEDimensoesDosIconesSaoPreservadas() throws Exception {
        Path arquivo = Files.createTempFile("khan-icones-", ".csv");
        try {
            Files.writeString(arquivo, "T;RUSSIA_OESTE;ESPADA;0.2;0.3;0.025\n"
                    + "C;KIEV;-;0.4;0.5;0.06\n");
            IconesTabuleiro dados = IconesTabuleiro.carregar(arquivo);
            dados.getTributos().get(0).posicionar(0.22, 0.31, 0.028);
            dados.getCidades().get(0).posicionar(0.44, 0.55, 0.07);
            dados.salvar(arquivo);
            IconesTabuleiro restaurados = IconesTabuleiro.carregar(arquivo);
            assertEquals(0.22, restaurados.getTributos().get(0).getX(), 0.0000001);
            assertEquals(0.028, restaurados.getTributos().get(0).getRaio(), 0.0000001);
            assertEquals(0.55, restaurados.getCidades().get(0).getY(), 0.0000001);
            assertEquals(0.07, restaurados.getCidades().get(0).getRaio(), 0.0000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
