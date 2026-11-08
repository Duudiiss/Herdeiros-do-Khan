package br.herdeiros.layout;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

/** Verifica a integracao dos CSVs revisados e a quantidade de casas editaveis. */
public class MarcacoesOficiaisTest {

    /**
     * Objetivo: preservar as 17 casas reais do placar e cinco slots do mercado.
     * Condicao inicial: tabuleiro.csv atualizado pelas marcas enviadas pela equipe.
     * Resultado esperado: todas as casas consultaveis e ausencia de marcadores extras.
     */
    @Test(timeout = 2000)
    public void testeTabuleiroPossuiPontuacaoEMercadoSemProgressoFixo() throws Exception {
        LayoutMarcadores layout = new LayoutMarcadores(LayoutMarcadores.caminhoTabuleiro());
        for (int i = 0; i <= 16; i++) {
            assertNotNull("Casa da pontuacao deve estar mapeada: " + i,
                    layout.consultar("pontos." + i));
        }
        for (int i = 0; i < 5; i++) {
            assertNotNull("Espaco do mercado deve estar mapeado: " + i,
                    layout.consultar("mercado." + i));
        }
        assertNull("O ponto fim antigo nao deve ser preservado", layout.consultar("fim"));
        assertNull("O progresso nao deve ter coordenadas separadas", layout.consultar("progresso.0"));
    }

    /**
     * Objetivo: garantir quatro linhas de melhoria nas quatro colunas de cada cartela.
     * Condicao inicial: cartelas 1 a 5 com pontos configurados no editor.
     * Resultado esperado: 16 centros de melhoria e quatro centros de ativacao.
     */
    @Test(timeout = 2000)
    public void testeCincoCartelasPossuemQuatroLinhasDeMarcacao() throws Exception {
        for (int cartela = 1; cartela <= 5; cartela++) {
            LayoutMarcadores layout = new LayoutMarcadores(LayoutMarcadores.caminhoCartela(cartela));
            for (int coluna = 0; coluna < 4; coluna++) {
                for (int linha = 0; linha < 4; linha++) {
                    assertNotNull("Posicao de melhoria incompleta da cartela " + cartela,
                            layout.consultar("melhoria." + coluna + "." + linha));
                }
                assertNotNull("Falta ponto de ativacao da coluna " + coluna,
                        layout.consultar("ativacao." + coluna));
            }
        }
    }
}
