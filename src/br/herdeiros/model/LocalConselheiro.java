package br.herdeiros.model;

import java.util.List;

/** Representa cada uma das 6 paradas duplas e Karakorum com sua demanda e bonus. */
final class LocalConselheiro implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final int idParada;
    private final String nome;
    private TipoTesouro[] tesourosExigidos; // null = 3 tesouros quaisquer
    private final int votosConcedidos;
    private PecaBonus pecaBonus;

    LocalConselheiro(int idParada, String nome, TipoTesouro[] tesourosExigidos,
                     int votosConcedidos, PecaBonus pecaBonus) {
        this.idParada = idParada;
        this.nome = nome;
        this.tesourosExigidos = tesourosExigidos;
        this.votosConcedidos = votosConcedidos;
        this.pecaBonus = pecaBonus;
    }

    void alterarDemanda(TipoTesouro[] demanda) { this.tesourosExigidos = demanda.clone(); }

    TipoTesouro[] getTesourosExigidos() {
        return tesourosExigidos == null ? null : tesourosExigidos.clone();
    }

    int getIdParada() {
        return idParada;
    }

    String getNome() {
        return nome;
    }

    int getVotosConcedidos() {
        return votosConcedidos;
    }

    PecaBonus getPecaBonus() {
        return pecaBonus;
    }

    boolean temPecaBonus() {
        return pecaBonus != null;
    }

    PecaBonus recolherPecaBonus() {
        PecaBonus retirada = pecaBonus;
        pecaBonus = null;
        return retirada;
    }

    String getDescricaoDemanda() {
        if (tesourosExigidos == null) {
            return "3 tesouros quaisquer (1 voto)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tesourosExigidos.length; i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(tesourosExigidos[i].getDescricao());
        }
        sb.append(" (").append(votosConcedidos).append(votosConcedidos > 1 ? " votos)" : " voto)");
        return sb.toString();
    }

    boolean podeEntregar(List<TipoTesouro> mao, TipoTesouro virtualAtivo, boolean virtualUsado) {
        if (tesourosExigidos == null) {
            int total = mao.size() + (virtualAtivo != null && !virtualUsado ? 1 : 0);
            return total >= 3;
        }
        java.util.List<TipoTesouro> copia = new java.util.ArrayList<TipoTesouro>(mao);
        boolean usouVirtual = virtualUsado;
        for (TipoTesouro exigido : tesourosExigidos) {
            if (!copia.remove(exigido)) {
                if (!usouVirtual && virtualAtivo == exigido) {
                    usouVirtual = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /** Consome os tesouros e retorna true se utilizou o tesouro virtual da coluna ativa. */
    boolean consumirTesouros(List<TipoTesouro> mao, TipoTesouro virtualAtivo,
                             boolean virtualUsado, List<TipoTesouro> pilhaDescarte) {
        if (!podeEntregar(mao, virtualAtivo, virtualUsado)) {
            throw new IllegalStateException("Tesouros insuficientes para este conselheiro.");
        }
        if (tesourosExigidos == null) {
            int faltam = 3;
            if (!virtualUsado && virtualAtivo != null && mao.size() < 3) {
                faltam--;
                virtualUsado = true;
            }
            for (int i = 0; i < faltam; i++) {
                pilhaDescarte.add(mao.remove(0));
            }
            return virtualUsado;
        }
        for (TipoTesouro exigido : tesourosExigidos) {
            if (mao.remove(exigido)) {
                pilhaDescarte.add(exigido);
            } else if (!virtualUsado && virtualAtivo == exigido) {
                virtualUsado = true;
            }
        }
        return virtualUsado;
    }
}