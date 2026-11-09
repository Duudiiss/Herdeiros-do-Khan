package br.herdeiros.model;

/** Contrato do observador: recebe notificacoes de mudancas no estado da partida. */
public interface ObservadorPartida {

    void atualizar(ObservadoPartida observado);
}
