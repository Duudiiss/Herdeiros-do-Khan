package br.herdeiros.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/** Guarda o estoque finito e as paradas que dão acesso a uma província. */
final class ReservaProvincia implements Serializable {
    private static final long serialVersionUID = 1L;

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

    EnumMap<TipoTributo, Integer> getQuantidades() {
        return new EnumMap<TipoTributo, Integer>(quantidades);
    }

    static ReservaProvincia restaurar(TipoTributo tipo, Map<TipoTributo, Integer> quantidades,
                                      Set<Integer> acessos) {
        if (tipo == null || quantidades == null || acessos == null) {
            throw new IllegalArgumentException("Estado de província inválido");
        }
        int total = 0;
        for (TipoTributo opcao : TipoTributo.values()) {
            Integer quantidade = quantidades.get(opcao);
            if (quantidade == null || quantidade < 0) {
                throw new IllegalArgumentException("Quantidade de tributo inválida");
            }
            total += quantidade;
        }
        if (total > 3) {
            throw new IllegalArgumentException("Estoque de província inválido");
        }
        ReservaProvincia reserva = new ReservaProvincia(tipo, quantidades.get(tipo));
        for (TipoTributo opcao : TipoTributo.values()) {
            if (opcao != tipo && quantidades.get(opcao) > 0
                    && !reserva.adicionarTipo(opcao, quantidades.get(opcao))) {
                throw new IllegalArgumentException("Estoque adicional de província inválido");
            }
        }
        for (Integer acesso : acessos) {
            if (acesso == null || !reserva.adicionarAcesso(acesso)) {
                throw new IllegalArgumentException("Acesso de província inválido");
            }
        }
        return reserva;
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

    /** Repoe somente o recurso associado ao icone escolhido no editor. */
    boolean reporUma(TipoTributo recurso) {
        if (recurso == null || total() >= 3) {
            return false;
        }
        quantidades.put(recurso, quantidade(recurso) + 1);
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
