package br.herdeiros.model;

import java.util.HashMap;

/** Representa uma posição do mapa e as paradas diretamente conectadas a ela. */
class Parada {
    private final int id;
    private final TipoParada tipo;
    private final HashMap<Integer, Parada> adjacentes = new HashMap<>();

    Parada(int id, boolean dupla) {
        this(id, identificarTipo(dupla));
    }

    private static TipoParada identificarTipo(boolean dupla) {
        if (dupla) {
            return TipoParada.DUPLA;
        } else {
            return TipoParada.SIMPLES;
        }
    }

    Parada(int id, TipoParada tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de parada obrigatório");
        }

        this.id = id;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public boolean isDupla() {
        return tipo == TipoParada.DUPLA;
    }

    public TipoParada getTipo() {
        return tipo;
    }

    public int capacidade() {
        if (tipo == TipoParada.KARAKORUM) {
            return Integer.MAX_VALUE;
        }

        if (tipo == TipoParada.DUPLA) {
            return 2;
        } else {
            return 1;
        }
    }

    public void adicionarAdjacente(Parada parada) {
        if (parada != null && parada != this) {
            adjacentes.put(parada.getId(), parada);
        }
    }

    public boolean ehAdjacente(Parada parada) {
        if (parada == null) {
            return false;
        }

        return adjacentes.get(parada.getId()) == parada;
    }
}
