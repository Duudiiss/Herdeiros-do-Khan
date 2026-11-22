package br.herdeiros.model;

/** As 14 cartas oficiais de Conselheiro Aberto (4 usadas por partida, 1 voto cada). */
public enum CartaConselheiroAberto {
    QUATRO_ESPADAS("Primeiro a ter 4 espadas"),
    SEIS_YURTS("Primeiro a ter 6 yurts no mapa"),
    DEZ_YURTS("Primeiro a ter 10 yurts no mapa"),
    QUATRO_TESOUROS("Primeiro a ter 4 tesouros"),
    CINCO_TESOUROS_DIFERENTES("Primeiro a ter 5 tesouros diferentes"),
    UM_YURT_CADA_REGIAO("Primeiro a ter 1 yurt em cada regiao"),
    DOIS_YURTS_CADA_REGIAO("Primeiro a ter 2 yurts em cada regiao"),
    DOIS_BONUS("Primeiro a conseguir 2 pecas de bonus"),
    TRES_BONUS("Primeiro a conseguir 3 pecas de bonus"),
    DUAS_CIDADES("Primeiro a conquistar 2 cidades"),
    TRES_CIDADES("Primeiro a conquistar 3 cidades"),
    UMA_LINHA_MELHORIAS("Primeiro a completar 1 linha de melhorias"),
    DUAS_COLUNAS_DUAS_MELHORIAS("Primeiro a ter 2 colunas com 2 melhorias cada"),
    QUATRO_MELHORIAS_COLUNA("Primeiro a ter 4 melhorias em uma so coluna");

    private final String descricao;

    CartaConselheiroAberto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}