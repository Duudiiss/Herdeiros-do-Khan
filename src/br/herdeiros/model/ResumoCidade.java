package br.herdeiros.model;

/**
 * Retrato somente de leitura de uma cidade disponivel no tabuleiro.
 * A View nao recebe uma referencia ao objeto Cidade do Model.
 */
public final class ResumoCidade {

    private final String nome;
    private final String regiao;
    private final int tesourosRestantes;
    private final boolean conquistada;

    public ResumoCidade(String nome, String regiao, int tesourosRestantes,
            boolean conquistada) {
        if (nome == null || regiao == null || tesourosRestantes < 0) {
            throw new IllegalArgumentException("Dados de cidade invalidos");
        }
        this.nome = nome;
        this.regiao = regiao;
        this.tesourosRestantes = tesourosRestantes;
        this.conquistada = conquistada;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public int getTesourosRestantes() {
        return tesourosRestantes;
    }

    public boolean isConquistada() {
        return conquistada;
    }
}
