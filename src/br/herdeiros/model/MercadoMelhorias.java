package br.herdeiros.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mercado com cinco espacos; somente a acao do Khan preenche vagas. */
final class MercadoMelhorias {
    private final ArrayList<Melhoria> pilha = new ArrayList<Melhoria>();
    private final Melhoria[] disponiveis = new Melhoria[5];

    MercadoMelhorias() {
        this(new java.util.Random());
    }

    /** Permite sorteio reprodutivel nos testes, mantendo aleatoriedade no jogo. */
    MercadoMelhorias(java.util.Random sorteador) {
        if (sorteador == null) {
            throw new IllegalArgumentException("Sorteador obrigatorio");
        }
        String[] codigos = {
            "PER-V", "BOI-A", "CAS-A", "CAS-R", "CAS-V", "CAV-A", "CAV-R", "CAV-V",
            "CUI-A", "ESP-A", "ESP-R", "ESP-V", "MAO-A", "MAO-R", "MAO-V",
            "MOB-R", "MOE-A", "MOE-R", "MOE-V", "MOP-V"
        };
        for (String codigo : codigos) {
            String sigla = codigo.substring(0, 3);
            String descricao = descricaoDaMelhoria(sigla);
            int coluna = colunaDaCor(codigo.charAt(4));
            pilha.add(new Melhoria(descricao, 2, 0, codigo, coluna));
        }
        Collections.shuffle(pilha, sorteador);
        reporVazios();
    }

    private String descricaoDaMelhoria(String sigla) {
        if (sigla.equals("CAV")) {
            return "Dois cavalos extras";
        }
        if (sigla.equals("MAO")) {
            return "Um punho extra";
        }
        if (sigla.equals("MOE")) {
            return "Uma moeda virtual";
        }
        if (sigla.equals("ESP")) {
            return "Uma espada virtual";
        }
        if (sigla.equals("CAS")) {
            return "Um yurt virtual";
        }
        if (sigla.equals("BOI") || sigla.equals("PER") || sigla.equals("CUI")
                || sigla.equals("MOB") || sigla.equals("MOP")) {
            return "Um tesouro virtual";
        }
        return "Melhoria de " + sigla;
    }

    private int colunaDaCor(char cor) {
        if (cor == 'R') {
            return 0;
        }
        if (cor == 'V') {
            return 1;
        }
        return 2;
    }

    Melhoria consultar(int indice) {
        if (indice < 0 || indice >= disponiveis.length) {
            return null;
        }
        return disponiveis[indice];
    }

    Melhoria retirar(int indice) {
        Melhoria melhoria = consultar(indice);
        if (melhoria == null) {
            return null;
        }
        disponiveis[indice] = null;
        // Mova para a direita as melhorias ainda disponiveis (regra do manual).
        int destino = disponiveis.length - 1;
        for (int origem = disponiveis.length - 1; origem >= 0; origem--) {
            if (disponiveis[origem] != null) {
                disponiveis[destino] = disponiveis[origem];
                if (destino != origem) {
                    disponiveis[origem] = null;
                }
                destino--;
            }
        }
        return melhoria;
    }

    int reporVazios() {
        int adicionadas = 0;
        for (int indice = 0; indice < disponiveis.length; indice++) {
            if (disponiveis[indice] == null && !pilha.isEmpty()) {
                disponiveis[indice] = pilha.remove(pilha.size() - 1);
                adicionadas++;
            }
        }
        return adicionadas;
    }

    /** Retira do topo da pilha sem retirar nenhuma das cartas reveladas. */
    Melhoria retirarAleatoria() {
        if (pilha.isEmpty()) {
            return null;
        }
        return pilha.remove(pilha.size() - 1);
    }

    boolean possuiPilha() {
        return !pilha.isEmpty();
    }

    int quantidadeDisponivel() {
        int quantidade = 0;
        for (Melhoria melhoria : disponiveis) {
            if (melhoria != null) {
                quantidade++;
            }
        }
        return quantidade;
    }

    List<ResumoMelhoria> consultarResumos() {
        ArrayList<ResumoMelhoria> resumos = new ArrayList<ResumoMelhoria>();
        for (Melhoria melhoria : disponiveis) {
            if (melhoria == null) {
                resumos.add(null);
            } else {
                resumos.add(new ResumoMelhoria(melhoria.getCodigo(),
                        melhoria.getDescdescricao(), melhoria.getColuna()));
            }
        }
        return Collections.unmodifiableList(resumos);
    }
}
