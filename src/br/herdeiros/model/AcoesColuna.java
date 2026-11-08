package br.herdeiros.model;

/**
 * Icones impressos nas colunas das cinco cartelas oficiais.
 * Os indices sao: 0 vermelha, 1 verde, 2 amarela e 3 branca.
 * Somente os cavalos estao integrados a movimentacao nesta etapa.
 */
final class AcoesColuna {
    private final int cavalos;
    private final int punhos;
    private final boolean khan;
    private final boolean melhoria;
    private final boolean recuperacao;

    private AcoesColuna(int cavalos, int punhos, boolean khan,
                        boolean melhoria, boolean recuperacao) {
        this.cavalos = cavalos;
        this.punhos = punhos;
        this.khan = khan;
        this.melhoria = melhoria;
        this.recuperacao = recuperacao;
    }

    static AcoesColuna obter(Personagem personagem, int coluna) {
        if (personagem == null || coluna < 0 || coluna > 3) {
            throw new IllegalArgumentException("Personagem ou coluna invalida");
        }

        if (coluna == 3) {
            return new AcoesColuna(2, 0, true, false, true);
        }

        switch (personagem) {
            case ALTANI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 2, true, false, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(2, 1, false, false, false);
                }
                return new AcoesColuna(1, 1, false, true, false);
            case JOCHI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 1, false, true, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(1, 2, true, false, false);
                }
                return new AcoesColuna(2, 1, false, false, false);
            case CHAGATAI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 2, true, false, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(1, 1, false, true, false);
                }
                return new AcoesColuna(2, 1, false, false, false);
            case TOLUI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 1, false, true, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(2, 1, false, false, false);
                }
                return new AcoesColuna(1, 1, true, false, false);
            case OGEDEI:
                if (coluna == 0) {
                    return new AcoesColuna(2, 1, false, false, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(1, 1, false, true, false);
                }
                return new AcoesColuna(1, 2, true, false, false);
            default:
                throw new IllegalArgumentException("Personagem desconhecido");
        }
    }

    int getCavalos() {
        return cavalos;
    }

    int getPunhos() {
        return punhos;
    }

    boolean temKhan() {
        return khan;
    }

    boolean temMelhoria() {
        return melhoria;
    }

    boolean recuperaPecas() {
        return recuperacao;
    }
}
