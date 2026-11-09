package br.herdeiros.model;

/** Retrato imutavel de uma melhoria exibida pela Facade. */
public final class ResumoMelhoria {
    private final String codigo;
    private final String descricao;
    private final int coluna;

    public ResumoMelhoria(String codigo, String descricao, int coluna) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.coluna = coluna;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getColuna() {
        return coluna;
    }
}
