package br.herdeiros.model;

/** Representa um peão e mantém a referência à sua única posição atual. */
class Peao {
    private final Cor cor;
    private Parada paradaAtual;

    public Peao(Cor cor) {
        if (cor == null) {
            throw new IllegalArgumentException("Cor obrigatória");
        }

        this.cor = cor;
    }

    public Cor getCor() {
        return cor;
    }

    public String getLocalizacaoAtual() {
        String localizacao;

        if (paradaAtual == null) {
            localizacao = "Fora do tabuleiro";
        } else if (paradaAtual.getTipo() == TipoParada.KARAKORUM) {
            localizacao = "Karakorum";
        } else {
            localizacao = "Parada " + paradaAtual.getId();
        }

        return localizacao;
    }

    public Parada getParadaAtual() {
        return paradaAtual;
    }

    void posicionarEm(Parada novaParada) {
        this.paradaAtual = novaParada;
    }
}
