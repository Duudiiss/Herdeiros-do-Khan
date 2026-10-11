package br.herdeiros.model;

public class Melhoria {
    private String descricao;
    private Integer custoEmMoedas;
    private Integer votosBonus;

    public Melhoria(String descricao, Integer custoEmMoedas, Integer votosBonus) {
        this.descricao = descricao;
        this.custoEmMoedas = custoEmMoedas;
        this.votosBonus = votosBonus;
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