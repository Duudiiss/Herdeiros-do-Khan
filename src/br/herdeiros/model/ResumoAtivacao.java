package br.herdeiros.model;

/** Dados de leitura da ativacao de colunas. Nao expoe colecoes mutaveis. */
public final class ResumoAtivacao {
    private final boolean[] colunasOcupadas;
    private final int colunaAtiva;
    private final int movimentosRestantes;
    private final int punhosDaColuna;
    private final int moedasVirtuaisDaColuna;
    private final boolean acaoKhan;
    private final boolean acaoMelhoria;
    private final int[] cavalosPorColuna;

    ResumoAtivacao(boolean[] colunasOcupadas, int colunaAtiva,
                   int movimentosRestantes, int punhosDaColuna,
                   boolean acaoKhan, boolean acaoMelhoria,
                   int[] cavalosPorColuna, int moedasVirtuaisDaColuna) {
        this.colunasOcupadas = colunasOcupadas.clone();
        this.colunaAtiva = colunaAtiva;
        this.movimentosRestantes = movimentosRestantes;
        this.punhosDaColuna = punhosDaColuna;
        this.moedasVirtuaisDaColuna = moedasVirtuaisDaColuna;
        this.acaoKhan = acaoKhan;
        this.acaoMelhoria = acaoMelhoria;
        this.cavalosPorColuna = cavalosPorColuna.clone();
    }

    public boolean estaOcupada(int coluna) {
        if (coluna < 0 || coluna >= colunasOcupadas.length) {
            throw new IllegalArgumentException("Coluna invalida");
        }
        return colunasOcupadas[coluna];
    }

    public boolean podeAtivar(int coluna) {
        if (coluna < 0 || coluna >= colunasOcupadas.length) {
            return false;
        }
        return colunaAtiva < 0 && !colunasOcupadas[coluna];
    }

    public int getColunaAtiva() {
        return colunaAtiva;
    }

    public int getMovimentosRestantes() {
        return movimentosRestantes;
    }

    public int getPunhosDaColuna() {
        return punhosDaColuna;
    }

    public int getMoedasVirtuaisDaColuna() {
        return moedasVirtuaisDaColuna;
    }

    public boolean temAcaoKhan() {
        return acaoKhan;
    }

    public boolean temAcaoMelhoria() {
        return acaoMelhoria;
    }

    public int getCavalosDaColuna(int coluna) {
        if (coluna < 0 || coluna >= cavalosPorColuna.length) {
            throw new IllegalArgumentException("Coluna invalida");
        }
        return cavalosPorColuna[coluna];
    }
}
