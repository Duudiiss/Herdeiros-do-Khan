package br.herdeiros.model;

/** As 10 cartas oficiais de Conselheiro Secreto (avaliadas de 0 a 3 votos no fim). */
public enum CartaConselheiroSecreto {
    VIAJANTE("Viajante: grupo conectado de 3 / 6 / 8 yurts"),
    SOLDADO("Soldado: ter 4 / 7 / 10 pecas de melhoria"),
    COLECIONADOR("Colecionador: ter 4 / 6 / 7 melhorias com simbolos diferentes"),
    SACERDOTE("Sacerdote: ter 1 / 2 / 4 pares de melhorias com o mesmo simbolo"),
    GOVERNADOR("Governador: 1 / 2 / 3 regioes com >=2 yurts e >=2 melhorias na coluna"),
    BAJULADOR("Bajulador: >=2 yurts adjacentes a 2 / 3 / 4 provincias de Khan"),
    PREFEITO("Prefeito: >=2 yurts adjacentes a 2 / 3 / 4 cidades"),
    CIDADAO("Cidadao: >=1 yurt adjacente a 3 / 6 / 8 cidades"),
    CAVALEIRO("Cavaleiro: 2 / 4 / 5 melhorias com simbolo de cavalo ou punho"),
    MENSAGEIRO("Mensageiro: >=1 yurt em 2 / 4 / 6 paradas duplas");

    private final String descricao;

    CartaConselheiroSecreto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public int calcularVotos(int valorAlcancado, int meta1, int meta2, int meta3) {
        if (valorAlcancado >= meta3) {
            return 3;
        }
        if (valorAlcancado >= meta2) {
            return 2;
        }
        if (valorAlcancado >= meta1) {
            return 1;
        }
        return 0;
    }
}