package br.herdeiros.mapa;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;

/** Testes dos raios clicáveis, escala e persistência do mapa. */
public class RaioParadaTest {
    private MapaDados mapa;

    @Before
    public void preparar() {
        mapa = new MapaDados();
        mapa.adicionar(1, 0.30, 0.30, false);
        mapa.adicionar(2, 0.70, 0.70, true);
    }

    /** Uma parada dupla deve possuir área maior que uma simples. */
    @Test(timeout = 2000)
    public void testeDuplaPossuiRaioMaior() {
        assertTrue("Parada dupla deve oferecer área clicável maior",
                mapa.getPonto(2).getRaio() > mapa.getPonto(1).getRaio());
    }

    /** O mesmo ponto normalizado deve ser detectado em tamanhos distintos. */
    @Test(timeout = 2000)
    public void testeCliqueEscalaComJanela() {
        assertEquals("Parada deve ser encontrada em janela pequena", 1,
                mapa.encontrarParada(0.315, 0.30, 500, 500));
        assertEquals("Parada deve ser encontrada em janela maior", 1,
                mapa.encontrarParada(0.315, 0.30, 1000, 1000));
    }

    /** Parada dupla marcada entre dois símbolos aceita cliques próximos ao centro. */
    @Test(timeout = 2000)
    public void testeRaioMaiorDaDupla() {
        assertEquals("Dupla deve responder dentro do seu raio ampliado", 2,
                mapa.encontrarParada(0.735, 0.70, 700, 700));
    }

    /** O raio personalizado deve alterar a região de seleção. */
    @Test(timeout = 2000)
    public void testeRaioPersonalizado() {
        assertEquals("Antes do ajuste clique está fora da área", -1,
                mapa.encontrarParada(0.36, 0.30, 700, 700));
        mapa.alterarRaio(1, 0.07);
        assertEquals("Depois do ajuste clique está dentro da área", 1,
                mapa.encontrarParada(0.36, 0.30, 700, 700));
    }

    /** O salvamento deve manter o raio definido no editor. */
    @Test(timeout = 2000)
    public void testeSalvarECarregarRaio() throws Exception {
        mapa.alterarRaio(2, 0.063);
        Path arquivo = Files.createTempFile("khan-raio", ".csv");
        try {
            ArquivoMapa.salvar(mapa, arquivo);
            MapaDados restaurado = ArquivoMapa.carregar(arquivo);
            assertEquals("Raio salvo deve ser restaurado", 0.063,
                    restaurado.getPonto(2).getRaio(), 0.000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** O leitor deve continuar aceitando o arquivo legado sem coluna de raio. */
    @Test(timeout = 2000)
    public void testeCompatibilidadeArquivoAntigo() throws Exception {
        Path arquivo = Files.createTempFile("khan-legado", ".csv");
        try {
            Files.writeString(arquivo, "P;0;0.5;0.5;false\nP;1;0.3;0.3;true\nC;0;1\n");
            MapaDados restaurado = ArquivoMapa.carregar(arquivo);
            assertEquals("Mapa antigo deve receber raio padrão de parada dupla",
                    MapaDados.raioPadrao(1, true), restaurado.getPonto(1).getRaio(), 0.000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
