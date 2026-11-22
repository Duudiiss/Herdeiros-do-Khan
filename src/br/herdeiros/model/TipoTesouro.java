package br.herdeiros.model;

/** Os 5 tipos de tesouro saqueados nas cidades (8 pecas de cada = 40 no total). */
public enum TipoTesouro {
    PELES("Peles"),
    SEDA("Seda"),
    PORCELANA("Porcelana"),
    FERRO("Ferro"),
    GADO("Gado");

    private final String descricao;

    TipoTesouro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}