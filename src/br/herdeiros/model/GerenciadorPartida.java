package br.herdeiros.model;

/** Singleton: ponto unico de acesso a partida ativa. */
public final class GerenciadorPartida {

    private static final GerenciadorPartida INSTANCIA = new GerenciadorPartida();
    private InterfacePartida partidaAtual;

    private GerenciadorPartida() {
    }

    public static GerenciadorPartida getInstancia() {
        return INSTANCIA;
    }

    /** Uma nova partida substitui a anterior, nao criando outro gerenciador. */
    public InterfacePartida iniciarPartida(String[] nomes, Cor[] cores) {
        InterfacePartida novaPartida = new InterfacePartida(nomes, cores);
        partidaAtual = novaPartida;
        return partidaAtual;
    }

    public InterfacePartida getPartidaAtual() {
        return partidaAtual;
    }
}
