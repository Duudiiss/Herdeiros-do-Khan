package br.herdeiros.model;

/** Retrato imutavel de um local de conselheiro (parada dupla ou Karakorum). */
public final class ResumoConselheiro {
    private final int idParada;
    private final String nome;
    private final String demanda;
    private final int votos;
    private final String bonusDisponivel;
    private final String imagemBonus;
    private final boolean jogadorPresente;
    private final boolean podeEntregar;

    public ResumoConselheiro(int idParada, String nome, String demanda, int votos,
                             String bonusDisponivel, String imagemBonus, boolean jogadorPresente, boolean podeEntregar) {
        this.idParada = idParada;
        this.nome = nome;
        this.demanda = demanda;
        this.votos = votos;
        this.bonusDisponivel = bonusDisponivel;
        this.imagemBonus = imagemBonus;
        this.jogadorPresente = jogadorPresente;
        this.podeEntregar = podeEntregar;
    }

    public int getIdParada() { return idParada; }
    public String getNome() { return nome; }
    public String getDemanda() { return demanda; }
    public int getVotos() { return votos; }
    public String getImagemBonus() { return imagemBonus; }
    public String getBonusDisponivel() { return bonusDisponivel; }
    public boolean isJogadorPresente() { return jogadorPresente; }
    public boolean isPodeEntregar() { return podeEntregar; }
}