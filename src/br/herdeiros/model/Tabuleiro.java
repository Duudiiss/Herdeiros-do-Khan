package br.herdeiros.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Armazena as posições, conexões e peças e verifica movimentação e ocupação. */
class Tabuleiro {
    private final Peao peaoDoKhan;
    private final List<Cidade> cidadesDisponiveis;
    private final HashMap<Integer, Parada> paradas = new HashMap<>();
    private final Set<Peao> peoes = Collections.newSetFromMap(new IdentityHashMap<Peao, Boolean>());
    private final HashMap<Integer, Cor> yurts = new HashMap<>();

    public Tabuleiro(List<Cidade> cidadesIniciaisSorteadas) {
        if (cidadesIniciaisSorteadas == null) {
            throw new IllegalArgumentException("Lista de cidades obrigatória");
        }

        this.peaoDoKhan = new Peao(Cor.BRANCO);
        this.cidadesDisponiveis = new ArrayList<>(cidadesIniciaisSorteadas);
        this.paradas.put(0, new Parada(0, TipoParada.KARAKORUM));
    }

    public boolean adicionarParada(int id, boolean dupla) {
        if (id <= 0 || paradas.containsKey(id)) {
            return false;
        }

        paradas.put(id, new Parada(id, dupla));
        return true;
    }

    public boolean conectarParadas(int id1, int id2) {
        Parada primeira = paradas.get(id1);
        Parada segunda = paradas.get(id2);

        if (primeira == null || segunda == null || primeira == segunda) {
            return false;
        }

        primeira.adicionarAdjacente(segunda);
        segunda.adicionarAdjacente(primeira);
        return true;
    }

    public boolean saoAdjacentes(int id1, int id2) {
        Parada primeira = paradas.get(id1);
        Parada segunda = paradas.get(id2);

        if (primeira == null) {
            return false;
        }

        if (segunda == null) {
            return false;
        }

        return primeira.ehAdjacente(segunda);
    }

    public void substituirCidadeConquistada(Cidade cidadeAntiga, Cidade novaCidade) {
        if (cidadeAntiga != null) {
            boolean removida = cidadesDisponiveis.remove(cidadeAntiga);
            if (removida && novaCidade != null) {
                cidadesDisponiveis.add(novaCidade);
            }
        }
    }

    public Peao getPeaoDoKhan() {
        return peaoDoKhan;
    }

    public List<Cidade> getCidadesDisponiveis() {
        return new ArrayList<>(cidadesDisponiveis);
    }

    public int getQuantidadeParadas() {
        return paradas.size();
    }

    /** Registra o peão de um jogador em Karakorum somente uma vez. */
    public boolean posicionarPeaoInicial(Peao peao) {
        if (peao == null || peao == peaoDoKhan || peao.getCor() == Cor.BRANCO || peoes.contains(peao)) {
            return false;
        }

        peoes.add(peao);
        peao.posicionarEm(paradas.get(0));
        return true;
    }

    private int ocupacao(Parada parada, Peao ignorar) {
        int quantidade = 0;

        for (Peao peao : peoes) {
            if (peao != ignorar && peao.getParadaAtual() == parada) {
                quantidade++;
            }
        }

        if (peaoDoKhan != ignorar && peaoDoKhan.getParadaAtual() == parada) {
            quantidade++;
        }

        return quantidade;
    }

    public int getOcupacao(int id) {
        Parada parada = paradas.get(id);
        if (parada == null) {
            return -1;
        }

        return ocupacao(parada, null);
    }

    /** Executa um único movimento entre paradas conectadas, sem gastar ativações. */
    public boolean moverPeao(Peao peao, int idDestino) {
        if (peao == null || !peoes.contains(peao)) {
            return false;
        }

        Parada origem = peao.getParadaAtual();
        Parada destino = paradas.get(idDestino);
        if (origem == null || destino == null) {
            return false;
        }

        if (!origem.ehAdjacente(destino)) {
            return false;
        }

        if (ocupacao(destino, peao) >= destino.capacidade()) {
            return false;
        }

        peao.posicionarEm(destino);
        return true;
    }

    /** O Khan pode entrar a partir de fora; movimentos seguintes exigem conexão. */
    public boolean moverKhan(int idDestino) {
        Parada destino = paradas.get(idDestino);
        if (destino == null) {
            return false;
        }

        Parada origem = peaoDoKhan.getParadaAtual();
        if (origem != null && !origem.ehAdjacente(destino)) {
            return false;
        }

        if (ocupacao(destino, peaoDoKhan) >= destino.capacidade()) {
            return false;
        }

        peaoDoKhan.posicionarEm(destino);
        return true;
    }

    /** Registra uma yurt em uma parada válida que ainda não tenha yurt. */
    public boolean construirYurt(int idParada, Cor cor) {
        Parada parada = paradas.get(idParada);
        if (parada == null || cor == null || cor == Cor.BRANCO) {
            return false;
        }

        if (parada.getTipo() == TipoParada.KARAKORUM) {
            return false;
        }

        if (yurts.containsKey(idParada)) {
            return false;
        }

        yurts.put(idParada, cor);
        return true;
    }

    public Cor getDonoYurt(int idParada) {
        return yurts.get(idParada);
    }
}
