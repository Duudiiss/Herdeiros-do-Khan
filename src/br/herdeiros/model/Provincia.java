package br.herdeiros.model;

/** Setores de tributos acessíveis aos peões e ao Khan. */
public enum Provincia {
    RUSSIA_OESTE(DestinoKhan.RUSSIA_OESTE),
    RUSSIA_LESTE(DestinoKhan.RUSSIA_LESTE),
    PERSIA_SUL(DestinoKhan.PERSIA_SUL),
    CHINA_LESTE(DestinoKhan.CHINA_LESTE);

    private final DestinoKhan destino;

    Provincia(DestinoKhan destino) {
        this.destino = destino;
    }

    public DestinoKhan getDestino() {
        return destino;
    }

    /** Vizinhança circular configurada para o demonstrador; deve ser revisada no mapa. */
    public Provincia[] vizinhas() {
        if (this == RUSSIA_OESTE) {
            return new Provincia[] {RUSSIA_LESTE, PERSIA_SUL};
        }
        if (this == RUSSIA_LESTE) {
            return new Provincia[] {RUSSIA_OESTE, CHINA_LESTE};
        }
        if (this == PERSIA_SUL) {
            return new Provincia[] {RUSSIA_OESTE, CHINA_LESTE};
        }
        return new Provincia[] {RUSSIA_LESTE, PERSIA_SUL};
    }

    @Override
    public String toString() {
        return destino.getDescricao();
    }
}
