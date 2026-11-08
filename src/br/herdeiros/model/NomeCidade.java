package br.herdeiros.model;

enum NomeCidade {
    KIEV("Kiev"),
    MOSCOU("Moscou"),
    SARAI("Sarai"),
    SAMARCANDA("Samarcanda"),
    BAGDA("Bagdá"),
    CABUL("Cabul"),
    KAESONG("Kaesong"),
    PEQUIM("Pequim"),
    CANTAO("Cantão");

    private final String nomeFormatado;

    NomeCidade(String nomeFormatado) {
        this.nomeFormatado = nomeFormatado;
    }

    public String getNomeFormatado() {
        return nomeFormatado;
    }
}
