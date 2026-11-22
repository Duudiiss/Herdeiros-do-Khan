package br.herdeiros.model;

/** As 7 pecas de bonus de uso unico posicionadas nos 7 locais de conselheiro. */
public enum PecaBonus {
    VOTO_IMEDIATO("+1 voto imediato na trilha do Kurultai", "cb_1votoKurultai.jpg"),
    DUAS_ESPADAS("Receba 2 pecas de espada", "cb_2espadas.jpg"),
    DUAS_MOEDAS("Receba 2 pecas de moeda", "cb_2moedas.jpg"),
    DOIS_YURTS("Receba 2 pecas de yurt", "cb_2yurts.jpg"),
    TRES_MOVIMENTOS_UM_PUNHO("Faca 3 movimentos extras e 1 coleta de tributo", "cb_3cavalos1punho.jpg"),
    DOIS_PUNHOS("Faca 2 coletas extras de tributo", "cb_2punhos.jpg"),
    ESCOLHER_TESOURO("Compre 3 tesouros da pilha e mantenha 1", "cb_3tesouros.jpg");

    private final String descricao;
    private final String arquivoImagem;

    PecaBonus(String descricao, String arquivoImagem) {
        this.descricao = descricao;
        this.arquivoImagem = arquivoImagem;
    }

    public String getArquivoImagem() { return arquivoImagem; }

    public String getDescricao() {
        return descricao;
    }
}
