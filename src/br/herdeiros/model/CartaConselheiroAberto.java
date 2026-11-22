package br.herdeiros.model;

/** As 14 cartas oficiais de Conselheiro Aberto (4 usadas por partida, 1 voto cada). */
public enum CartaConselheiroAberto {
    QUATRO_ESPADAS("Primeiro a ter 4 espadas", "ca_4espadas.jpg"),
    SEIS_YURTS("Primeiro a ter 6 yurts no mapa", "ca_6yurts.jpg"),
    DEZ_YURTS("Primeiro a ter 10 yurts no mapa", "ca_10yurts.jpg"),
    QUATRO_TESOUROS("Primeiro a ter 4 tesouros", "ca_4tesouros.jpg"),
    CINCO_TESOUROS_DIFERENTES("Primeiro a ter 5 tesouros diferentes", "ca_1tesourodecada.jpg"),
    UM_YURT_CADA_REGIAO("Primeiro a ter 1 yurt em cada regiao", "ca_1yc-1yr-1yp.jpg"),
    DOIS_YURTS_CADA_REGIAO("Primeiro a ter 2 yurts em cada regiao", "ca_2yc-2yr-2yp.jpg"),
    DOIS_BONUS("Primeiro a conseguir 2 pecas de bonus", "ca_2bonus.jpg"),
    TRES_BONUS("Primeiro a conseguir 3 pecas de bonus", "ca_3bonus.jpg"),
    DUAS_CIDADES("Primeiro a conquistar 2 cidades", "ca_conq2cidades.jpg"),
    TRES_CIDADES("Primeiro a conquistar 3 cidades", "ca_conq3cidades.jpg"),
    UMA_LINHA_MELHORIAS("Primeiro a completar 1 linha de melhorias", "ca_3melhoriashorizontal.jpg"),
    DUAS_COLUNAS_DUAS_MELHORIAS("Primeiro a ter 2 colunas com 2 melhorias cada", "ca_4melhorias2x2.jpg"),
    QUATRO_MELHORIAS_COLUNA("Primeiro a ter 4 melhorias em uma so coluna", "ca_4melhoriasverticais.jpg");

    private final String descricao;
    private final String arquivoImagem;

    CartaConselheiroAberto(String descricao, String arquivoImagem) {
        this.descricao = descricao;
        this.arquivoImagem = arquivoImagem;
    }

    public String getArquivoImagem() {
        return arquivoImagem;
    }

    public String getDescricao() {
        return descricao;
    }
}