package br.herdeiros.model;

/** Contrato do observado, conforme a estrutura Observer do capitulo 17. */
public interface ObservadoPartida {

    void registrarObservador(ObservadorPartida observador);

    void removerObservador(ObservadorPartida observador);

    /** Permite ao observador consultar o estado apos a notificacao. */
    int getIndiceJogadorAtual();

    int getParadaJogador(int indice);
}
