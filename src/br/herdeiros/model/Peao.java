package br.herdeiros.model;

/** Representa um peão; o tabuleiro coordena a validação de seus movimentos. */
class Peao {
    private final Cor cor;
    private String localizacaoAtual;
    private Parada paradaAtual;

    public Peao(Cor cor, String localizacaoInicial) {
        if (cor == null) {
            throw new IllegalArgumentException("Cor obrigatória");
        }

        this.cor = cor;
        this.localizacaoAtual = localizacaoInicial;
    }

    public Cor getCor() {
        return cor;
    }

    public String getLocalizacaoAtual() {
        return localizacaoAtual;
    }

    public Parada getParadaAtual() {
        return paradaAtual;
    }

    void posicionarEm(Parada novaParada) {
        this.paradaAtual = novaParada;

        if (novaParada == null) {
            this.localizacaoAtual = "Fora do tabuleiro";
        } else if (novaParada.getTipo() == TipoParada.KARAKORUM) {
            this.localizacaoAtual = "Karakorum";
        } else {
            this.localizacaoAtual = "Parada " + novaParada.getId();
        }
    }
}
