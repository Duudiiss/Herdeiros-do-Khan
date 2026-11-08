package br.herdeiros.model;

/** Destinos exclusivos do Khan: quatro províncias e área de melhorias. */
public enum DestinoKhan {
    RUSSIA_OESTE("Província russa oeste", 0.213, 0.210),
    RUSSIA_LESTE("Província russa leste", 0.605, 0.302),
    PERSIA_SUL("Província persa", 0.276, 0.591),
    CHINA_LESTE("Província chinesa", 0.742, 0.693),
    MELHORIAS("Área de melhorias", 0.036, 0.955);

    private final String descricao;
    private final double x;
    private final double y;

    DestinoKhan(String descricao, double x, double y) {
        this.descricao = descricao;
        this.x = x;
        this.y = y;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean ehProvincia() {
        return this != MELHORIAS;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
