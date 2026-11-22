package br.herdeiros.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;
import org.junit.Test;

/**
 * Testes da ocultacao de informacao exigida na primeira iteracao.
 * O Model disponibiliza a fachada e tipos de leitura, nao seus objetos internos.
 */
public class EncapsulamentoModelTest {

    /** Objetivo: ocultar os elementos internos do jogo dos outros pacotes. */
    @Test
    public void classesBasicasNaoSaoPublicas() {
        Class<?>[] internas = {
            Conselheiro.class, Melhoria.class, Tributo.class,
            TabuleiroIndividual.class, SavePartidaData.class,
            Jogador.class, Jogo.class, Tabuleiro.class,
            Cidade.class, Parada.class, Peao.class, LocalConselheiro.class
        };
        for (Class<?> classe : internas) {
            assertFalse(classe.getSimpleName() + " nao deve ser publica",
                    Modifier.isPublic(classe.getModifiers()));
        }
    }

    /** Objetivo: manter a API e o mecanismo Observer acessiveis a View/Controller. */
    @Test
    public void fachadaEContratosPermanecemPublicos() {
        assertTrue(Modifier.isPublic(InterfacePartida.class.getModifiers()));
        assertTrue(Modifier.isPublic(GerenciadorPartida.class.getModifiers()));
        assertTrue(Modifier.isPublic(GerenciadorSave.class.getModifiers()));
        assertTrue(Modifier.isPublic(ObservadoPartida.class.getModifiers()));
        assertTrue(Modifier.isPublic(ObservadorPartida.class.getModifiers()));
        assertTrue(Modifier.isPublic(ResumoCartela.class.getModifiers()));
    }
}
