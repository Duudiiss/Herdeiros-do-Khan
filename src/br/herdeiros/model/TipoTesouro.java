package br.herdeiros.model;

/** Os 5 tipos de tesouro saqueados nas cidades (8 pecas de cada = 40 no total). */
public enum TipoTesouro {
    PELES("Especiarias"),
    SEDA("Seda"),
    PORCELANA("Peles"),
    FERRO("Ferro"),
    GADO("Gado");

    private final String descricao;

    TipoTesouro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}