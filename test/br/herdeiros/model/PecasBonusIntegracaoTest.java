package br.herdeiros.model;

import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import br.herdeiros.mapa.ArquivoMapa;

/** Testes JUnit 4: distribuicao, historico, uso unico, escolha e save. */
public class PecasBonusIntegracaoTest {
    private InterfacePartida partidaComMapa() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        assertTrue(partida.carregarMapa(ArquivoMapa.carregar(Paths.get("assets/mapa_paradas.csv"))));
        return partida;
    }

    @Test public void distribuicaoNaoRepeteNemIncluiEspecial() throws Exception {
        InterfacePartida p = partidaComMapa();
        List<ResumoConselheiro> locais = p.consultarConselheiros();
        assertEquals(7, locais.size());
        Set<String> imagens = new HashSet<String>();
        for (ResumoConselheiro c : locais) {
            assertNotNull(c.getImagemBonus());
            assertTrue(imagens.add(c.getImagemBonus()));
            assertTrue(Files.isRegularFile(Paths.get("assets/cartas_bonus", c.getImagemBonus())));
        }
        assertFalse(imagens.contains("cb_karakorum.jpg"));
    }

    @Test public void consumoConservaHistoricoEImpedeSegundoUso() throws Exception {
        InterfacePartida p = partidaComMapa();
        assertTrue(p.desenvolvimentoAjustarItemEspecial(0, "BONUS_DUAS_ESPADAS", 1));
        int i = p.consultarBonusJogadorAtual().indexOf(PecaBonus.DUAS_ESPADAS);
        assertTrue(p.usarPecaBonusJogadorAtual(i));
        assertTrue(p.isBonusUtilizadoJogadorAtual(i));
        assertFalse(p.usarPecaBonusJogadorAtual(i));
        assertEquals(1, p.consultarBonusJogadorAtual().size());
    }

    @Test public void escolhaTesouroNaoConsomeAntesDeSelecionar() throws Exception {
        InterfacePartida p = partidaComMapa();
        assertTrue(p.desenvolvimentoAjustarItemEspecial(0, "BONUS_ESCOLHER_TESOURO", 1));
        int i = p.consultarBonusJogadorAtual().indexOf(PecaBonus.ESCOLHER_TESOURO);
        assertTrue(p.usarPecaBonusJogadorAtual(i));
        assertEquals(3, p.consultarEscolhaTesourosBonusPendente().size());
        assertFalse(p.isBonusUtilizadoJogadorAtual(i));
        assertTrue(p.escolherTesouroBonus(1));
        assertTrue(p.isBonusUtilizadoJogadorAtual(i));
        assertEquals(1, p.consultarQuantidadeTesourosJogadorAtual());
    }

    @Test public void cincoEspadasCompletaObjetivoDeQuatroSemIgualdadeExata() throws Exception {
        InterfacePartida p = partidaComMapa();
        java.lang.reflect.Field ativasCampo = InterfacePartida.class.getDeclaredField("cartasAbertasAtivas");
        ativasCampo.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<CartaConselheiroAberto> ativas = (List<CartaConselheiroAberto>) ativasCampo.get(p);
        ativas.clear();
        java.lang.reflect.Method revelar = InterfacePartida.class.getDeclaredMethod(
                "revelarCartaAberta", CartaConselheiroAberto.class);
        revelar.setAccessible(true);
        revelar.invoke(p, CartaConselheiroAberto.QUATRO_ESPADAS);
        int antes = p.consultarVotosJogador(0);
        assertTrue(p.simularRecebimentoTributo(0, "ESPADA", 5));
        assertTrue(p.consultarVotosJogador(0) >= antes + 1);
        assertFalse(p.consultarCartasAbertasAtivas().contains(CartaConselheiroAberto.QUATRO_ESPADAS));
    }

    @Test public void savePreservaHistoricoEConfiguracaoEspecial() throws Exception {
        InterfacePartida p = partidaComMapa();
        assertTrue(p.configurarKarakorumEspecial(true));
        assertTrue(p.desenvolvimentoAjustarItemEspecial(0, "BONUS_DUAS_MOEDAS", 1));
        assertTrue(p.usarPecaBonusJogadorAtual(0));
        Path destino = Files.createTempFile("bonus-khan", ".txt");
        try {
            GerenciadorSave.salvar(destino, p);
            InterfacePartida recarregado = GerenciadorSave.carregar(destino);
            assertEquals(1, recarregado.consultarBonusJogadorAtual().size());
            assertTrue(recarregado.isBonusUtilizadoJogadorAtual(0));
            assertTrue(recarregado.isKarakorumEspecial());
        } finally {
            Files.deleteIfExists(destino);
        }
    }
}
