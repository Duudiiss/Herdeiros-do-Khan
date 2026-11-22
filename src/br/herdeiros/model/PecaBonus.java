package br.herdeiros.model;

/** As 7 pecas de bonus de uso unico posicionadas nos 7 locais de conselheiro. */
public enum PecaBonus {
    VOTO_IMEDIATO("+1 voto imediato na trilha do Kurultai"),
    DUAS_ESPADAS("Receba 2 pecas de espada"),
    DUAS_MOEDAS("Receba 2 pecas de moeda"),
    DOIS_YURTS("Receba 2 pecas de yurt"),
    TRES_MOVIMENTOS_UM_PUNHO("Faca 3 movimentos extras e 1 coleta de tributo"),
    DOIS_PUNHOS("Faca 2 coletas extras de tributo"),
    ESCOLHER_TESOURO("Compre 3 tesouros da pilha e mantenha 1");

    private final String descricao;

    PecaBonus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}