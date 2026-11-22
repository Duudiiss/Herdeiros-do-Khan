package br.herdeiros.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mercado com cinco espacos; somente a acao do Khan preenche vagas. */
final class MercadoMelhorias implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<Melhoria> pilha;
    private final Melhoria[] disponiveis;

    MercadoMelhorias() {
        this(new java.util.Random());
    }

    /** Permite sorteio reprodutivel nos testes, mantendo aleatoriedade no jogo. */
    MercadoMelhorias(java.util.Random sorteador) {
        if (sorteador == null) {
            throw new IllegalArgumentException("Sorteador obrigatorio");
        }
        pilha = new ArrayList<Melhoria>();
        disponiveis = new Melhoria[5];
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

    MercadoMelhorias(List<Melhoria> melhoriasRestantes, List<Melhoria> melhoriasDisponiveis) {
        if (melhoriasRestantes == null || melhoriasDisponiveis == null
                || melhoriasDisponiveis.size() != 5) {
            throw new IllegalArgumentException("Estado do mercado inválido");
        }
        pilha = new ArrayList<Melhoria>(melhoriasRestantes);
        disponiveis = melhoriasDisponiveis.toArray(new Melhoria[5]);
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
            return "Um tesouro virtual: " + descricaoTesouroVirtual(sigla);
        }
        return "Melhoria de " + sigla;
    }

    private static String descricaoTesouroVirtual(String sigla) {
        switch (sigla) {
            case "BOI": return "Gado";
            case "CUI": return "Especiarias";
            case "MOP": return "Peles";
            case "PER": return "Seda";
            case "MOB": return "Ferro";
            default: return "desconhecido";
        }
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

    static Melhoria criarPorCodigo(String codigo, int coluna) {
        String[] codigosValidos = {
            "PER-V", "BOI-A", "CAS-A", "CAS-R", "CAS-V", "CAV-A", "CAV-R", "CAV-V",
            "CUI-A", "ESP-A", "ESP-R", "ESP-V", "MAO-A", "MAO-R", "MAO-V",
            "MOB-R", "MOE-A", "MOE-R", "MOE-V", "MOP-V"
        };
        if (codigo == null || !java.util.Arrays.asList(codigosValidos).contains(codigo)
                || coluna < 0 || coluna > 3) {
            throw new IllegalArgumentException("Código de melhoria inválido");
        }
        String sigla = codigo.substring(0, 3);
        String descricao;
        if (sigla.equals("CAV")) {
            descricao = "Dois cavalos extras";
        } else if (sigla.equals("MAO")) {
            descricao = "Um punho extra";
        } else if (sigla.equals("MOE")) {
            descricao = "Uma moeda virtual";
        } else if (sigla.equals("ESP")) {
            descricao = "Uma espada virtual";
        } else if (sigla.equals("CAS")) {
            descricao = "Um yurt virtual";
        } else if (sigla.equals("BOI") || sigla.equals("PER") || sigla.equals("CUI")
                || sigla.equals("MOB") || sigla.equals("MOP")) {
            descricao = "Um tesouro virtual: " + descricaoTesouroVirtual(sigla);
        } else {
            throw new IllegalArgumentException("Código de melhoria desconhecido: " + codigo);
        }
        return new Melhoria(descricao, 2, 0, codigo, coluna);
    }

    List<Melhoria> getMelhoriasRestantes() {
        return new ArrayList<Melhoria>(pilha);
    }

    List<Melhoria> getMelhoriasDisponiveis() {
        return new ArrayList<Melhoria>(java.util.Arrays.asList(disponiveis));
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

    /** Devolve a melhoria nao escolhida por Jochi ao fundo do baralho. */
    void devolverAoFundo(Melhoria melhoria) {
        if (melhoria != null) {
            pilha.add(0, melhoria);
        }
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
