package br.herdeiros.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Organiza uma partida de dois a cinco jogadores humanos. */
class Jogo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final HashMap<Cor, Jogador> jogadores = new HashMap<>();
    private final BaralhoDeCidades baralho;
    private final Tabuleiro tabuleiro;

    public Jogo(List<Jogador> participantes) {
        this(participantes, new BaralhoDeCidades(), null);
    }

    Jogo(List<Jogador> participantes, BaralhoDeCidades baralho, Tabuleiro tabuleiro) {
        if (participantes == null || participantes.size() < 2 || participantes.size() > 5) {
            throw new IllegalArgumentException("A partida requer de 2 a 5 jogadores");
        }
        if (baralho == null) {
            throw new IllegalArgumentException("Baralho obrigatório");
        }

        for (Jogador jogador : participantes) {
            if (jogador == null) {
                throw new IllegalArgumentException("Jogador ausente");
            }
            if (jogador.getCor() == Cor.BRANCO || jogadores.containsKey(jogador.getCor())) {
                throw new IllegalArgumentException("Cor repetida ou inválida");
            }

            jogadores.put(jogador.getCor(), jogador);
        }

        this.baralho = baralho;
        this.tabuleiro = tabuleiro == null
                ? new Tabuleiro(baralho.sacarCidadesIniciais()) : tabuleiro;

        for (Jogador jogador : participantes) {
            if (jogador.getMeuPeao().getParadaAtual() == null
                    && !this.tabuleiro.posicionarPeaoInicial(jogador.getMeuPeao())) {
                throw new IllegalArgumentException("Não foi possível posicionar o jogador");
            }
        }
    }

    Cidade revelarProximaCidade() {
        return baralho.sacarCidadeDoTopo();
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    BaralhoDeCidades getBaralho() {
        return baralho;
    }

    public int getQuantidadeJogadores() {
        return jogadores.size();
    }

    public Jogador getJogador(Cor cor) {
        return jogadores.get(cor);
    }

    /** Retorna todos os jogadores empatados na maior pontuação disponível. */
    public List<Jogador> jogadoresComMaisVotos() {
        List<Jogador> vencedores = new ArrayList<>();
        int maiorQuantidade = Integer.MIN_VALUE;

        for (Jogador jogador : jogadores.values()) {
            if (jogador.getVotosKurultai() > maiorQuantidade) {
                vencedores.clear();
                maiorQuantidade = jogador.getVotosKurultai();
            }

            if (jogador.getVotosKurultai() == maiorQuantidade) {
                vencedores.add(jogador);
            }
        }

        return vencedores;
    }
}
