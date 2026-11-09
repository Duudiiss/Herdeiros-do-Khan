package br.herdeiros.model;

public class Melhoria {
    private String descricao;
    private Integer custoEmMoedas;
    private Integer votosBonus;
    private final String codigo;
    private final int coluna;

    public Melhoria(String descricao, Integer custoEmMoedas, Integer votosBonus) {
        this.descricao = descricao;
        this.custoEmMoedas = custoEmMoedas;
        this.votosBonus = votosBonus;
        this.codigo = null;
        this.coluna = -1;
    }

    public Melhoria(String descricao, Integer custoEmMoedas, Integer votosBonus,
                    String codigo, int coluna) {
        this.descricao = descricao;
        this.custoEmMoedas = custoEmMoedas;
        this.votosBonus = votosBonus;
        this.codigo = codigo;
        this.coluna = coluna;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getColuna() {
        return coluna;
    }

    public String getDescdescricao() {
        return descricao;
    }

    public Integer getCustoEmMoedas() {
        return custoEmMoedas;
    }

    public Integer getVotosBonus() {
        return votosBonus;
    }
}