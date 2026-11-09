package br.herdeiros.model;

/** Personagens presentes nas cinco cartelas oficiais, sem vínculo com a cor. */
public enum Personagem {
    ALTANI("Altani"),
    JOCHI("Jochi"),
    CHAGATAI("Chagatai"),
    TOLUI("Tolui"),
    OGEDEI("Ogedei");

    private final String nomeExibicao;

    private Personagem(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    /** Correspondência direta com cartela1.jpg ... cartela5.jpg. */
    public int getIndiceCartela() {
        return ordinal();
    }
}
