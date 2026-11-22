package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Regressao das regras de cartela, fichas expostas e conselheiros fixos. */
public class RegrasFinaisCartelaTesourosTest {
    @Test
    public void melhoriaCompradaNaoBloqueiaAtivacaoDaMesmaColuna() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(new Melhoria("Cavalos", 2, 0, "CAV1", 0));
        assertTrue(cartela.consultarAtivacao(Personagem.ALTANI).podeAtivar(0));
        assertTrue(cartela.consultarAtivacao(Personagem.ALTANI).podeAtivar(1));
        cartela.encerrarTurno();
        assertTrue(cartela.consultarAtivacao(Personagem.ALTANI).podeAtivar(0));
    }

    @Test
    public void conselheirosTemDemandasFixasPorParada() {
        InterfacePartida partida = new InterfacePartida(2);
        MapaDados mapa = new MapaDados();
        for (int id : new int[]{23,28,29,30,31,32,33}) {
            mapa.adicionar(id, .1 + (id - 20) * .03, .3, true);
            mapa.conectar(0, id);
        }
        assertTrue(partida.carregarMapa(mapa));
        java.util.Map<Integer, String> descricoes = new java.util.HashMap<>();
        for (ResumoConselheiro c : partida.consultarConselheiros()) {
            descricoes.put(c.getIdParada(), c.getDemanda());
        }
        assertTrue(descricoes.get(29).contains("Gado + Gado"));
        assertTrue(descricoes.get(23).contains("3 tesouros quaisquer"));
        assertTrue(descricoes.get(33).contains("3 tesouros quaisquer"));
        assertTrue(descricoes.get(30).contains("Peles + Peles"));
        assertTrue(descricoes.get(32).contains("Especiarias + Especiarias"));
        assertTrue(descricoes.get(31).contains("Seda + Seda"));
        assertTrue(descricoes.get(28).contains("Ferro + Ferro"));
        assertTrue(descricoes.get(0).contains("Ferro + Peles + Gado"));
    }

    @Test
    public void fichasDeCidadeVisiveisSaoPreservadasPeloSave() throws Exception {
        InterfacePartida original = new InterfacePartida(2);
        List<ResumoCidade> abertas = original.consultarCidadesDisponiveis();
        assertFalse(abertas.isEmpty());
        String cidade = abertas.get(0).getNome();
        List<TipoTesouro> fichas = original.consultarTesourosCidade(cidade);
        assertEquals(abertas.get(0).getTesourosRestantes(), fichas.size());
        Path caminho = Files.createTempFile("khan-fichas-", ".txt");
        try {
            GerenciadorSave.salvar(caminho, original);
            InterfacePartida recuperada = GerenciadorSave.carregar(caminho);
            assertEquals(fichas, recuperada.consultarTesourosCidade(cidade));
        } finally {
            Files.deleteIfExists(caminho);
        }
    }
}
