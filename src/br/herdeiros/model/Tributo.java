package br.herdeiros.model;

public class Tributo {
    private TipoTributo tipo;

    public Tributo(TipoTributo tipo) {
        this.tipo = tipo;
    }

    public TipoTributo getTipo() {
        return tipo;
    }
}