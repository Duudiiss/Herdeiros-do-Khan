package br.herdeiros.model;

import java.util.HashSet;
import java.util.EnumMap;
import java.util.Set;

/** Guarda o estoque finito e as paradas que dão acesso a uma província. */
final class ReservaProvincia {
    private final TipoTributo tipo;
    private final Set<Integer> acessos = new HashSet<Integer>();
    private final EnumMap<TipoTributo, Integer> quantidades =
            new EnumMap<TipoTributo, Integer>(TipoTributo.class);

    ReservaProvincia(TipoTributo tipo, int quantidade) {
        if (tipo == null || quantidade < 0 || quantidade > 3) {
            throw new IllegalArgumentException("Dados de província inválidos");
        }
        this.tipo = tipo;
        for (TipoTributo opcao : TipoTributo.values()) {
            quantidades.put(opcao, 0);
        }
        quantidades.put(tipo, quantidade);
    }

    boolean adicionarAcesso(int id) {
        if (id < 0) {
            return false;
        }
        return acessos.add(id);
    }

    boolean acessivelPelas(Set<Integer> visitadas) {
        for (Integer parada : acessos) {
            if (visitadas.contains(parada)) {
                return true;
            }
        }
        return false;
    }

    Set<Integer> getAcessos() {
        return new HashSet<Integer>(acessos);
    }

    TipoTributo getTipo() {
        return tipo;
    }

    int getQuantidade() {
        return quantidade(tipo);
    }

    int quantidade(TipoTributo tipoConsultado) {
        Integer valor = quantidades.get(tipoConsultado);
        if (valor == null) {
            return 0;
        }
        return valor;
    }

    int total() {
        int soma = 0;
        for (Integer valor : quantidades.values()) {
            soma += valor;
        }
        return soma;
    }

    boolean adicionarTipo(TipoTributo outroTipo, int quantidade) {
        if (outroTipo == null || quantidade < 0 || total() + quantidade > 3) {
            return false;
        }
        if (quantidades.get(outroTipo) > 0) {
            return false;
        }
        quantidades.put(outroTipo, quantidade);
        return true;
    }

    boolean retirar() {
        return retirar(tipo);
    }

    boolean retirar(TipoTributo selecionado) {
        if (quantidade(selecionado) < 1) {
            return false;
        }
        quantidades.put(selecionado, quantidade(selecionado) - 1);
        return true;
    }

    boolean reporUma() {
        if (total() >= 3) {
            return false;
        }
        // Prioriza o tipo principal original; a escolha definitiva de reposicao
        // depende das regras de sorteio dos tributos, ainda nao automatizadas.
        quantidades.put(tipo, quantidade(tipo) + 1);
        return true;
    }
}
