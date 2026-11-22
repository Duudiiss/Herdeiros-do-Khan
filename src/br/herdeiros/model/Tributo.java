package br.herdeiros.model;

import java.io.Serializable;

public class Tributo implements Serializable {
    private static final long serialVersionUID = 1L;

    private TipoTributo tipo;

    public Tributo(TipoTributo tipo) {
        this.tipo = tipo;
    }

    public TipoTributo getTipo() {
        return tipo;
    }
}