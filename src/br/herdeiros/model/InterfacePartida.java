package br.herdeiros.model;

import java.util.ArrayList;
import java.util.List;
import br.herdeiros.mapa.MapaDados;

/** API mínima para a interface da segunda iteração; não expõe as classes internas. */
public class InterfacePartida {
    private final Jogo jogo;
    private final List<Jogador> participantes;

    public InterfacePartida() {
        this(new String[] {"Azul", "Vermelho"}, new Cor[] {Cor.AZUL, Cor.VERMELHO});
    }

    public InterfacePartida(int quantidadeJogadores) {
        this(criarNomesJogadores(quantidadeJogadores), criarCoresJogadores(quantidadeJogadores));
    }

    public InterfacePartida(String[] nomes, Cor[] cores) {
        if (nomes == null || cores == null || nomes.length == 0 || nomes.length != cores.length) {
            throw new IllegalArgumentException("Cada jogador deve ter nome e cor");
        }
        participantes = new ArrayList<>();
        for (int i = 0; i < nomes.length; i++) {
            participantes.add(new Jogador(nomes[i], cores[i], 0));
        }
        jogo = new Jogo(participantes);
    }

    private static String[] criarNomesJogadores(int quantidadeJogadores) {
        if (quantidadeJogadores < 2 || quantidadeJogadores > 5) {
            throw new IllegalArgumentException("A partida requer de 2 a 5 jogadores");
        }

        String[] nomes = new String[quantidadeJogadores];
        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = "Jogador " + (i + 1);
        }
        return nomes;
    }

    private static Cor[] criarCoresJogadores(int quantidadeJogadores) {
        Cor[] coresDisponiveis = {Cor.AZUL, Cor.VERMELHO, Cor.AMARELO,
                Cor.VERDE, Cor.LARANJA};
        Cor[] cores = new Cor[quantidadeJogadores];
        System.arraycopy(coresDisponiveis, 0, cores, 0, quantidadeJogadores);
        return cores;
    }

    /** Carrega um mapa de uma só vez; não deixa um tabuleiro parcialmente configurado. */
    public boolean carregarMapa(MapaDados mapa) {
        if (mapa == null || jogo.getTabuleiro().getQuantidadeParadas() != 1) {
            return false;
        }
        for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
            if (ponto.getId() != 0 && !jogo.getTabuleiro().adicionarParada(
                    ponto.getId(), ponto.isDupla())) {
                return false;
            }
        }
        for (MapaDados.Ligacao ligacao : mapa.getLigacoes()) {
            if (!jogo.getTabuleiro().conectarParadas(ligacao.getOrigem(), ligacao.getDestino())) {
                return false;
            }
        }
        return true;
    }

    public boolean cadastrarParada(int id, boolean dupla) {
        return jogo.getTabuleiro().adicionarParada(id, dupla);
    }

    public boolean conectarParadas(int origem, int destino) {
        return jogo.getTabuleiro().conectarParadas(origem, destino);
    }

    public boolean movimentarJogador(int indice, int destino) {
        if (indice < 0 || indice >= participantes.size()) {
            return false;
        }
        return jogo.getTabuleiro().moverPeao(participantes.get(indice).getMeuPeao(), destino);
    }

    private int indiceAtual = 0;

    /** Jogador ativo da rodada visual. */
    public int getIndiceJogadorAtual() {
        return indiceAtual;
    }

    /**
     * Executa uma ação de movimento com o número de movimentos informado.
     * O seletor da interface é provisório, até implementar a ativação de cavalos.
     * Um clique inválido não modifica o jogador ativo.
     */
    public boolean movimentarJogadorAtual(int destino, int movimentosDisponiveis) {
        Peao peao = participantes.get(indiceAtual).getMeuPeao();
        boolean realizado = jogo.getTabuleiro().moverPeaoAte(peao, destino, movimentosDisponiveis);
        if (realizado) {
            avancarJogador();
        }
        return realizado;
    }

    /** Passa para o próximo participante na ordem de cadastro. */
    public void avancarJogador() {
        indiceAtual++;
        if (indiceAtual >= participantes.size()) {
            indiceAtual = 0;
        }
    }

    public int getParadaJogador(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return -1;
        }
        Parada parada = participantes.get(indice).getMeuPeao().getParadaAtual();
        if (parada == null) {
            return -1;
        }
        return parada.getId();
    }

    public int getQuantidadeJogadores() {
        return participantes.size();
    }

    public String getNomeJogador(int indice) {
        return participantes.get(indice).getNome();
    }

    public Cor getCorJogador(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return null;
        }
        return participantes.get(indice).getCor();
    }

    public int getOcupacao(int id) {
        return jogo.getTabuleiro().getOcupacao(id);
    }
}
