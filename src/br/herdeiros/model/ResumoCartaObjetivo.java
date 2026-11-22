package br.herdeiros.model;

import java.util.Collections;
import java.util.List;

/** Retrato imutavel das cartas de objetivo (abertas, secreta e bonus) do jogador atual. */
public final class ResumoCartaObjetivo {
    private final List<String> cartasAbertasDisponiveis;
    private final String cartaSecretaEscolhida;
    private final List<String> opcoesSecretasIniciais;
    private final List<String> bonusDisponiveisJogador;
    private final List<String> tesourosJogador;
    private final int cidadesConquistadas;

    public ResumoCartaObjetivo(List<String> cartasAbertasDisponiveis, String cartaSecretaEscolhida,
                               List<String> opcoesSecretasIniciais, List<String> bonusDisponiveisJogador,
                               List<String> tesourosJogador, int cidadesConquistadas) {
        this.cartasAbertasDisponiveis = Collections.unmodifiableList(cartasAbertasDisponiveis);
        this.cartaSecretaEscolhida = cartaSecretaEscolhida;
        this.opcoesSecretasIniciais = Collections.unmodifiableList(opcoesSecretasIniciais);
        this.bonusDisponiveisJogador = Collections.unmodifiableList(bonusDisponiveisJogador);
        this.tesourosJogador = Collections.unmodifiableList(tesourosJogador);
        this.cidadesConquistadas = cidadesConquistadas;
    }

    public List<String> getCartasAbertasDisponiveis() { return cartasAbertasDisponiveis; }
    public String getCartaSecretaEscolhida() { return cartaSecretaEscolhida; }
    public List<String> getOpcoesSecretasIniciais() { return opcoesSecretasIniciais; }
    public List<String> getBonusDisponiveisJogador() { return bonusDisponiveisJogador; }
    public List<String> getTesourosJogador() { return tesourosJogador; }
    public int getCidadesConquistadas() { return cidadesConquistadas; }
}