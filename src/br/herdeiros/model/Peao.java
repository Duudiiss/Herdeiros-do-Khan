package br.herdeiros.model;

/** Representa um peão; o tabuleiro coordena a validação de seus movimentos. */
class Peao {
    private final Cor cor;
    private Parada paradaAtual;

    public Peao(Cor cor) {
        if (cor == null) {
            throw new IllegalArgumentException("Cor obrigatória");
        }

        this.cor = cor;
        this.paradaAtual = new Parada(0, TipoParada.KARAKORUM);
    }

    public Cor getCor() {
        return cor;
    }

    public Parada getParadaAtual() {
        return paradaAtual;
    }

    void posicionarEm(Parada novaParada) {
        this.paradaAtual = novaParada;
    }
}
