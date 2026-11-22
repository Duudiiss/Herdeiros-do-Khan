package br.herdeiros.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Calcula votos de influencia regional, conselheiro secreto e criterios de desempate. */
final class AvaliadorPontuacaoFinal {

    // Tabela oficial (indice = melhorias na coluna 0..4)
    private static final int[] VOTOS_PRIMEIRO = {1, 1, 2, 2, 3};
    private static final int[] VOTOS_SEGUNDO  = {0, 1, 1, 2, 2};

    private AvaliadorPontuacaoFinal() {
    }

    static List<ResumoResultadoFinal> apurar(
            List<Jogador> jogadores,
            Tabuleiro tabuleiro,
            Map<Jogador, List<TipoTesouro>> tesourosPorJogador,
            Map<Jogador, CartaConselheiroSecreto> secretaPorJogador,
            Map<Jogador, List<ResumoMelhoria>> melhoriasPorJogador,
            Map<Jogador, Peao> peaoExtraAltaniPorJogador,
            Map<Integer, Regiao> regiaoPorParada,
            Map<NomeCidade, Regiao> regiaoPorCidade,
            Map<DestinoKhan, Set<Integer>> paradasPorProvinciaKhan,
            Map<NomeCidade, Set<Integer>> paradasPorCidade) {

        Map<Jogador, Integer> votosRegiao = new HashMap<Jogador, Integer>();
        for (Jogador j : jogadores) {
            votosRegiao.put(j, 0);
        }

        // 1. Influencia nas 3 regioes (China=0/Vermelha, Persia=1/Verde, Russia=2/Amarela)
        apurarRegiao(Regiao.CHINA, 0, jogadores, tabuleiro, melhoriasPorJogador,
                regiaoPorParada, regiaoPorCidade, votosRegiao);
        apurarRegiao(Regiao.PERSIA, 1, jogadores, tabuleiro, melhoriasPorJogador,
                regiaoPorParada, regiaoPorCidade, votosRegiao);
        apurarRegiao(Regiao.RUSSIA, 2, jogadores, tabuleiro, melhoriasPorJogador,
                regiaoPorParada, regiaoPorCidade, votosRegiao);

        // 2. Conselheiro secreto + montagem dos resultados individuais
        List<ResumoResultadoFinal> lista = new ArrayList<ResumoResultadoFinal>();
        for (Jogador j : jogadores) {
            int vTrilha = j.getVotosKurultai();
            int vReg = votosRegiao.get(j);
            CartaConselheiroSecreto cartaSecreta =
                    secretaPorJogador != null ? secretaPorJogador.get(j) : null;
            List<ResumoMelhoria> melhoriasJogador = obterMelhorias(melhoriasPorJogador, j);
            List<TipoTesouro> tesourosJogador = obterTesouros(tesourosPorJogador, j);

            int vSec = avaliarConselheiroSecreto(
                    j, cartaSecreta, melhoriasJogador, tabuleiro,
                    regiaoPorParada, regiaoPorCidade,
                    paradasPorProvinciaKhan, paradasPorCidade);

            int total = vTrilha + vReg + vSec;
            int tesourosRestantes = tesourosJogador.size();
            int distKarakorum = menorDistanciaKarakorum(j, peaoExtraAltaniPorJogador, tabuleiro);
            String nomePersonagem = j.getPersonagem() != null ? j.getPersonagem().name() : "Herdeiro";

            lista.add(new ResumoResultadoFinal(
                    j.getNome(),
                    j.getCor(),
                    nomePersonagem,
                    vTrilha,
                    vReg,
                    vSec,
                    total,
                    tesourosRestantes,
                    distKarakorum,
                    cartaSecreta != null ? cartaSecreta.getDescricao() : "Nenhuma",
                    false));
        }

        // 3. Ordenacao por votos totais -> tesouros nao entregues -> menor distancia de Karakorum
        Collections.sort(lista, new Comparator<ResumoResultadoFinal>() {
            @Override
            public int compare(ResumoResultadoFinal a, ResumoResultadoFinal b) {
                if (b.getVotosTotais() != a.getVotosTotais()) {
                    return Integer.compare(b.getVotosTotais(), a.getVotosTotais());
                }
                if (b.getTesourosNaoEntregues() != a.getTesourosNaoEntregues()) {
                    return Integer.compare(b.getTesourosNaoEntregues(), a.getTesourosNaoEntregues());
                }
                return Integer.compare(a.getDistanciaKarakorum(), b.getDistanciaKarakorum());
            }
        });

        // Marca todos os jogadores empatados no topo como vencedores
        if (!lista.isEmpty()) {
            ResumoResultadoFinal primeiro = lista.get(0);
            List<ResumoResultadoFinal> finalizados = new ArrayList<ResumoResultadoFinal>();
            for (ResumoResultadoFinal item : lista) {
                boolean venceu = item.getVotosTotais() == primeiro.getVotosTotais()
                        && item.getTesourosNaoEntregues() == primeiro.getTesourosNaoEntregues()
                        && item.getDistanciaKarakorum() == primeiro.getDistanciaKarakorum();
                finalizados.add(item.comVencedor(venceu));
            }
            return finalizados;
        }
        return lista;
    }

    private static void apurarRegiao(
            Regiao regiao,
            int colunaCorrespondente,
            List<Jogador> jogadores,
            Tabuleiro tabuleiro,
            Map<Jogador, List<ResumoMelhoria>> melhoriasPorJogador,
            Map<Integer, Regiao> regiaoPorParada,
            Map<NomeCidade, Regiao> regiaoPorCidade,
            Map<Jogador, Integer> votosRegiao) {

        Map<Jogador, Integer> contagem = new HashMap<Jogador, Integer>();
        int maior = 0;
        for (Jogador j : jogadores) {
            int qtd = tabuleiro.contarYurtsJogadorNaRegiao(
                    j.getCor(), regiao, regiaoPorParada, regiaoPorCidade);
            contagem.put(j, qtd);
            if (qtd > maior) {
                maior = qtd;
            }
        }
        if (maior <= 0) {
            return;
        }

        List<Jogador> primeiros = new ArrayList<Jogador>();
        int segundoMaior = 0;
        for (Jogador j : jogadores) {
            int qtd = contagem.get(j);
            if (qtd == maior) {
                primeiros.add(j);
            } else if (qtd > segundoMaior) {
                segundoMaior = qtd;
            }
        }

        for (Jogador p : primeiros) {
            int melhorias = Math.min(4,
                    contarMelhoriasColuna(obterMelhorias(melhoriasPorJogador, p), colunaCorrespondente));
            votosRegiao.put(p, votosRegiao.get(p) + VOTOS_PRIMEIRO[melhorias]);
        }

        // Empate no 1o lugar anula o 2o lugar
        if (primeiros.size() == 1 && segundoMaior > 0) {
            for (Jogador j : jogadores) {
                if (contagem.get(j) == segundoMaior) {
                    int melhorias = Math.min(4,
                            contarMelhoriasColuna(obterMelhorias(melhoriasPorJogador, j), colunaCorrespondente));
                    votosRegiao.put(j, votosRegiao.get(j) + VOTOS_SEGUNDO[melhorias]);
                }
            }
        }
    }

    private static int menorDistanciaKarakorum(
            Jogador j,
            Map<Jogador, Peao> peaoExtraAltaniPorJogador,
            Tabuleiro tabuleiro) {

        int melhor = Integer.MAX_VALUE;
        Peao principal = j.getMeuPeao();
        if (principal != null && principal.getParadaAtual() != null) {
            melhor = Math.min(melhor,
                    tabuleiro.distanciaMinimaParaKarakorum(principal.getParadaAtual().getId()));
        }
        if (peaoExtraAltaniPorJogador != null) {
            Peao extra = peaoExtraAltaniPorJogador.get(j);
            if (extra != null && extra.getParadaAtual() != null) {
                melhor = Math.min(melhor,
                        tabuleiro.distanciaMinimaParaKarakorum(extra.getParadaAtual().getId()));
            }
        }
        return melhor;
    }

    static int avaliarConselheiroSecreto(
            Jogador j,
            CartaConselheiroSecreto carta,
            List<ResumoMelhoria> melhorias,
            Tabuleiro tabuleiro,
            Map<Integer, Regiao> regiaoPorParada,
            Map<NomeCidade, Regiao> regiaoPorCidade,
            Map<DestinoKhan, Set<Integer>> paradasPorProvinciaKhan,
            Map<NomeCidade, Set<Integer>> paradasPorCidade) {

        if (carta == null) {
            return 0;
        }
        Cor cor = j.getCor();
        switch (carta) {
            case VIAJANTE:
                return carta.calcularVotos(tabuleiro.maiorRedeConectadaYurts(cor), 3, 6, 8);
            case SOLDADO:
                return carta.calcularVotos(melhorias.size(), 4, 7, 10);
            case COLECIONADOR:
                return carta.calcularVotos(contarTiposDistintosMelhorias(melhorias), 4, 6, 7);
            case SACERDOTE:
                return carta.calcularVotos(contarParesMelhoriasIguais(melhorias), 1, 2, 4);
            case GOVERNADOR: {
                int regioes = 0;
                Regiao[] r = {Regiao.CHINA, Regiao.PERSIA, Regiao.RUSSIA};
                for (int col = 0; col < 3; col++) {
                    int y = tabuleiro.contarYurtsJogadorNaRegiao(cor, r[col], regiaoPorParada, regiaoPorCidade);
                    int m = contarMelhoriasColuna(melhorias, col);
                    if (y >= 2 && m >= 2) {
                        regioes++;
                    }
                }
                return carta.calcularVotos(regioes, 1, 2, 3);
            }
            case BAJULADOR:
                return carta.calcularVotos(
                        tabuleiro.contarProvinciasKhanComDoisYurts(cor, paradasPorProvinciaKhan), 2, 3, 4);
            case PREFEITO:
                return carta.calcularVotos(
                        paradasPorCidade != null
                                ? tabuleiro.contarCidadesComYurtsAdjacentes(cor, paradasPorCidade.values(), 2)
                                : 0,
                        2, 3, 4);
            case CIDADAO:
                return carta.calcularVotos(
                        paradasPorCidade != null
                                ? tabuleiro.contarCidadesComYurtsAdjacentes(cor, paradasPorCidade.values(), 1)
                                : 0,
                        3, 6, 8);
            case CAVALEIRO:
                return carta.calcularVotos(contarMelhoriasCavaloOuPunho(melhorias), 2, 4, 5);
            case MENSAGEIRO:
                return carta.calcularVotos(tabuleiro.contarParadasDuplasComYurt(cor), 2, 4, 6);
            default:
                return 0;
        }
    }

    private static List<ResumoMelhoria> obterMelhorias(
            Map<Jogador, List<ResumoMelhoria>> mapa, Jogador j) {
        if (mapa == null || !mapa.containsKey(j) || mapa.get(j) == null) {
            return Collections.emptyList();
        }
        return mapa.get(j);
    }

    private static List<TipoTesouro> obterTesouros(
            Map<Jogador, List<TipoTesouro>> mapa, Jogador j) {
        if (mapa == null || !mapa.containsKey(j) || mapa.get(j) == null) {
            return Collections.emptyList();
        }
        return mapa.get(j);
    }

    private static int contarMelhoriasColuna(List<ResumoMelhoria> melhorias, int coluna) {
        int total = 0;
        for (ResumoMelhoria m : melhorias) {
            if (m.getColuna() == coluna) {
                total++;
            }
        }
        return total;
    }

    private static int contarTiposDistintosMelhorias(List<ResumoMelhoria> melhorias) {
        Set<String> tipos = new HashSet<String>();
        for (ResumoMelhoria m : melhorias) {
            tipos.add(normalizarSimboloMelhoria(m));
        }
        return tipos.size();
    }

    private static int contarParesMelhoriasIguais(List<ResumoMelhoria> melhorias) {
        Map<String, Integer> freq = new HashMap<String, Integer>();
        for (ResumoMelhoria m : melhorias) {
            String chave = normalizarSimboloMelhoria(m);
            Integer atual = freq.get(chave);
            freq.put(chave, atual == null ? 1 : atual + 1);
        }
        int pares = 0;
        for (Integer qtd : freq.values()) {
            pares += qtd / 2;
        }
        return pares;
    }

    private static int contarMelhoriasCavaloOuPunho(List<ResumoMelhoria> melhorias) {
        int total = 0;
        for (ResumoMelhoria m : melhorias) {
            String desc = (m.getCodigo() + " " + m.getDescricao()).toLowerCase(Locale.ROOT);
            if (desc.contains("cav") || desc.contains("mao") || desc.contains("punho") || desc.contains("movimento")) {
                total++;
            }
        }
        return total;
    }

    private static String normalizarSimboloMelhoria(ResumoMelhoria m) {
        if (m.getDescricao() != null && !m.getDescricao().trim().isEmpty()) {
            return m.getDescricao().trim().toLowerCase(Locale.ROOT);
        }
        return m.getCodigo() != null ? m.getCodigo().trim().toLowerCase(Locale.ROOT) : "";
    }
}