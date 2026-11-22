package br.herdeiros.model;

/** Linha do placar final com detalhamento de votos e criterios de desempate. */
public final class ResumoResultadoFinal {
    private final String nomeJogador;
    private final Cor cor;
    private final String personagem;
    private final int votosTrilha;
    private final int votosRegioes;
    private final int votosConselheiroSecreto;
    private final int votosTotais;
    private final int tesourosNaoEntregues;
    private final int distanciaKarakorum;
    private final String descricaoCartaSecreta;
    private final boolean vencedor;
    private final int progressoConselheiro;
    private final String nomeConselheiro;

    public ResumoResultadoFinal(String nomeJogador, Cor cor, String personagem,
                                int votosTrilha, int votosRegioes, int votosConselheiroSecreto,
                                int votosTotais, int tesourosNaoEntregues, int distanciaKarakorum,
                                String descricaoCartaSecreta, boolean vencedor) {
        this(nomeJogador, cor, personagem, votosTrilha, votosRegioes, votosConselheiroSecreto,
                votosTotais, tesourosNaoEntregues, distanciaKarakorum,
                descricaoCartaSecreta, vencedor, 0, "Nenhum");
    }

    public ResumoResultadoFinal(String nomeJogador, Cor cor, String personagem,
                                int votosTrilha, int votosRegioes, int votosConselheiroSecreto,
                                int votosTotais, int tesourosNaoEntregues, int distanciaKarakorum,
                                String descricaoCartaSecreta, boolean vencedor,
                                int progressoConselheiro, String nomeConselheiro) {
        this.progressoConselheiro = progressoConselheiro;
        this.nomeConselheiro = nomeConselheiro;
        this.nomeJogador = nomeJogador;
        this.cor = cor;
        this.personagem = personagem;
        this.votosTrilha = votosTrilha;
        this.votosRegioes = votosRegioes;
        this.votosConselheiroSecreto = votosConselheiroSecreto;
        this.votosTotais = votosTotais;
        this.tesourosNaoEntregues = tesourosNaoEntregues;
        this.distanciaKarakorum = distanciaKarakorum;
        this.descricaoCartaSecreta = descricaoCartaSecreta;
        this.vencedor = vencedor;
    }

    ResumoResultadoFinal comVencedor(boolean ganhou) {
        return new ResumoResultadoFinal(nomeJogador, cor, personagem, votosTrilha, votosRegioes,
                votosConselheiroSecreto, votosTotais, tesourosNaoEntregues, distanciaKarakorum,
                descricaoCartaSecreta, ganhou, progressoConselheiro, nomeConselheiro);
    }

    public String getNomeJogador() { return nomeJogador; }
    public Cor getCor() { return cor; }
    public String getPersonagem() { return personagem; }
    public int getVotosTrilha() { return votosTrilha; }
    public int getVotosRegioes() { return votosRegioes; }
    public int getVotosConselheiroSecreto() { return votosConselheiroSecreto; }
    public int getVotosTotais() { return votosTotais; }
    public int getTesourosNaoEntregues() { return tesourosNaoEntregues; }
    public int getDistanciaKarakorum() { return distanciaKarakorum; }
    public String getDescricaoCartaSecreta() { return descricaoCartaSecreta; }
    public int getProgressoConselheiro() { return progressoConselheiro; }
    public String getNomeConselheiro() { return nomeConselheiro; }
    public boolean isVencedor() { return vencedor; }
}