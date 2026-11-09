package br.herdeiros.model;

/**
 * Acoes impressas nas cartelas oficiais, conferidas com a tabela da equipe.
 * 0 = vermelha, 1 = verde, 2 = amarela, 3 = branca.
 */
final class AcoesColuna {
    private final int cavalos;
    private final int punhos;
    private final int moedasVirtuais;
    private final boolean khan;
    private final boolean recuperacao;

    private AcoesColuna(int cavalos, int punhos, int moedasVirtuais,
                        boolean khan, boolean recuperacao) {
        this.cavalos = cavalos;
        this.punhos = punhos;
        this.moedasVirtuais = moedasVirtuais;
        this.khan = khan;
        this.recuperacao = recuperacao;
    }

    static AcoesColuna obter(Personagem personagem, int coluna) {
        if (personagem == null || coluna < 0 || coluna > 3) {
            throw new IllegalArgumentException("Personagem ou coluna invalida");
        }
        if (coluna == 3) {
            return new AcoesColuna(2, 0, 0, true, true);
        }

        switch (personagem) {
            case ALTANI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 2, 0, true, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(2, 1, 0, false, false);
                }
                return new AcoesColuna(1, 1, 1, false, false);
            case JOCHI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 1, 1, false, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(1, 2, 0, true, false);
                }
                return new AcoesColuna(2, 1, 0, false, false);
            case CHAGATAI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 2, 0, true, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(1, 1, 1, false, false);
                }
                return new AcoesColuna(2, 1, 0, false, false);
            case TOLUI:
                if (coluna == 0) {
                    return new AcoesColuna(1, 1, 1, false, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(2, 1, 0, false, false);
                }
                return new AcoesColuna(1, 2, 0, true, false);
            case OGEDEI:
                if (coluna == 0) {
                    return new AcoesColuna(2, 1, 0, false, false);
                }
                if (coluna == 1) {
                    return new AcoesColuna(1, 1, 1, false, false);
                }
                return new AcoesColuna(1, 2, 0, true, false);
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

    int getMoedasVirtuais() {
        return moedasVirtuais;
    }

    boolean temKhan() {
        return khan;
    }

    boolean recuperaPecas() {
        return recuperacao;
    }

    /** Compatibilidade com consultas anteriores: moeda virtual nao e compra gratuita. */
    boolean temMelhoria() {
        return false;
    }
}
