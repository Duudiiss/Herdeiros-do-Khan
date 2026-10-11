package br.herdeiros.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Organiza uma partida de dois a cinco jogadores humanos. */
class Jogo {
    private final HashMap<Cor, Jogador> jogadores = new HashMap<>();
    private final BaralhoDeCidades baralho;
    private final Tabuleiro tabuleiro;

    public Jogo(List<Jogador> participantes) {
        if (participantes == null || participantes.size() < 2 || participantes.size() > 5) {
            throw new IllegalArgumentException("A partida requer de 2 a 5 jogadores");
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

        baralho = new BaralhoDeCidades();
        tabuleiro = new Tabuleiro(baralho.sacarCidadesIniciais());

        for (Jogador jogador : participantes) {
            tabuleiro.posicionarPeaoInicial(jogador.getMeuPeao());
        }
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
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
