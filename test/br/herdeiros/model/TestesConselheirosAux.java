package br.herdeiros.model;

/** Prepara a escolha obrigatoria da carta secreta em testes de outras mecanicas. */
final class TestesConselheirosAux {
    private TestesConselheirosAux() {
    }

    static boolean avancar(InterfacePartida partida) {
        escolherSeNecessario(partida);
        return partida.tentarAvancarJogador();
    }

    static void avancarSemResultado(InterfacePartida partida) {
        escolherSeNecessario(partida);
        partida.avancarJogador();
    }

    static void escolherSeNecessario(InterfacePartida partida) {
        if (partida.consultarConselheiroSecretoJogadorAtual() == null
                && !partida.consultarOpcoesConselheiroSecretoJogadorAtual().isEmpty()) {
            partida.escolherConselheiroSecretoJogadorAtual(0);
        }
    }
}
