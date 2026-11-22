package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.layout.LayoutAreas;
import br.herdeiros.layout.LayoutRaios;
import br.herdeiros.layout.LayoutMarcadores;
import br.herdeiros.layout.LayoutMarcadores.Posicao;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.DestinoKhan;
import br.herdeiros.model.Provincia;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ObservadoPartida;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import br.herdeiros.mapa.MapaDados;
import br.herdeiros.mapa.IconesTabuleiro;
import br.herdeiros.model.ResumoCidade;
import br.herdeiros.model.ResumoMelhoria;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** O JPanel é somente a superfície: imagens são desenhadas com Graphics2D.drawImage. */
public final class PainelTabuleiro extends JPanel implements ObservadorPartida {
    private static final long serialVersionUID = 1L;

    private final BufferedImage imagem;
    private final BufferedImage[] imagemPeoes;
    private final BufferedImage imagemKhan;
    private final Map<String, BufferedImage> cacheMelhorias;
    private final Map<String, BufferedImage> cacheBonus = new HashMap<String, BufferedImage>();
    private final Map<br.herdeiros.model.TipoTesouro, BufferedImage> cacheTesouros;
    private final MapaDados mapa;
    private final IconesTabuleiro icones;
    private final LayoutMarcadores layoutMarcadores;
    private final LayoutAreas areasCartas;
    private final LayoutRaios raiosMarcadores;
    private final InterfacePartida partida;
    private final ControladorTabuleiro controlador;
    private int xImagem;
    private int yImagem;
    private int larguraImagem;
    private int alturaImagem;

    public PainelTabuleiro(InterfacePartida partida, MapaDados mapa,
                          ControladorTabuleiro controlador) throws IOException {
        this.partida = partida;
        this.mapa = mapa;
        this.icones = IconesTabuleiro.carregar(Paths.get("assets", "icones_tabuleiro.csv"));
        this.layoutMarcadores = new LayoutMarcadores(LayoutMarcadores.caminhoTabuleiro());
        this.areasCartas = new LayoutAreas(LayoutAreas.caminhoTabuleiro());
        this.raiosMarcadores = new LayoutRaios(LayoutRaios.caminhoTabuleiro());
        this.controlador = controlador;
        this.cacheMelhorias = new HashMap<String, BufferedImage>();
        this.cacheTesouros = new HashMap<br.herdeiros.model.TipoTesouro, BufferedImage>();
        imagem = ImageIO.read(new File("assets/tabuleiro.jpg"));
        imagemPeoes = new BufferedImage[partida.getQuantidadeJogadores()];
        imagemKhan = criarImagemKhan();
        for (int i = 0; i < imagemPeoes.length; i++) {
            imagemPeoes[i] = criarImagemPeao(CoresJogadores.obter(partida.getCorJogador(i)));
        }
        setPreferredSize(new Dimension(820, 820));
        setBackground(new Color(45, 43, 51));
        addMouseListener(controlador);
    }

    private BufferedImage criarImagemPeao(Color cor) {
        BufferedImage token = new BufferedImage(32, 36, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = token.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(30, 24, 17, 210));
        g.fillOval(5, 27, 22, 7);
        g.setColor(cor);
        g.fillOval(9, 2, 14, 14);
        g.fillRoundRect(7, 15, 18, 17, 5, 5);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(9, 2, 14, 14);
        g.dispose();
        return token;
    }
    
    private void drawMapa(Graphics graficos) {
    	Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double escala = Math.min((double) getWidth() / imagem.getWidth(),
                                 (double) getHeight() / imagem.getHeight());
        larguraImagem = (int) Math.round(imagem.getWidth() * escala);
        alturaImagem = (int) Math.round(imagem.getHeight() * escala);
        xImagem = (getWidth() - larguraImagem) / 2;
        yImagem = (getHeight() - alturaImagem) / 2;
        g.drawImage(imagem, xImagem, yImagem, larguraImagem, alturaImagem, null);
    }

    private BufferedImage criarImagemKhan() {
        BufferedImage marcador = new BufferedImage(52, 52, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = marcador.createGraphics();
        g.setColor(new Color(28, 24, 18, 190));
        g.fillOval(2, 2, 48, 48);
        g.setColor(new Color(244, 219, 139));
        g.setStroke(new BasicStroke(3.0f));
        g.drawOval(3, 3, 46, 46);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 15f));
        g.drawString("KH", 14, 31);
        g.dispose();
        return marcador;
    }
    
    private void drawPeoes(Graphics graficos) {
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double escala = Math.min((double) getWidth() / imagem.getWidth(),
                                 (double) getHeight() / imagem.getHeight());
        larguraImagem = (int) Math.round(imagem.getWidth() * escala);
        alturaImagem = (int) Math.round(imagem.getHeight() * escala);
        xImagem = (getWidth() - larguraImagem) / 2;
        yImagem = (getHeight() - alturaImagem) / 2;
        g.drawImage(imagem, xImagem, yImagem, larguraImagem, alturaImagem, null);
        desenharYurts(g);
        for (int i = 0; i < partida.getQuantidadeJogadores(); i++) {
            int principal = partida.consultarParadaPeaoPrincipal(i);
            int segundo = partida.consultarParadaSegundoPeaoAltani(i);
            desenharPeaoNaParada(g, i, principal, segundo >= 0 ? -14 : 0, "1");
            if (segundo >= 0) {
                desenharPeaoNaParada(g, i, segundo, 14, "2");
            }
        }
    }

    /** Altani mantem os dois peoes visiveis, mesmo quando ocupam a mesma parada. */
    private void desenharPeaoNaParada(Graphics2D g, int indice, int id,
                                      int deslocamentoExtra, String numero) {
        MapaDados.Ponto ponto = mapa.getPonto(id);
        if (ponto == null) {
            return;
        }
        int x = xImagem + (int) Math.round(ponto.getX() * larguraImagem);
        int y = yImagem + (int) Math.round(ponto.getY() * alturaImagem);
        int deslocamento = calcularDeslocamentoHorizontal(indice, id) + deslocamentoExtra;
        g.drawImage(imagemPeoes[indice], x + deslocamento - 16, y - 47, 32, 36, null);
        if (partida.consultarParadaSegundoPeaoAltani(indice) >= 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString(numero, x + deslocamento - 3, y - 17);
        }
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        drawMapa(graficos);
        drawPeoes(graficos);
        desenharContadores(g);
        desenharMercadoMelhorias(g);
        desenharPecasBonus(g);
        desenharMarcadoresPontuacao(g);
        desenharDestinosDisponiveisKhan(g);
        DestinoKhan destinoKhan = partida.consultarDestinoKhan();
        if (destinoKhan != null) {
            double[] posicaoKhan = posicaoDestinoKhan(destinoKhan);
            int x = xImagem + (int) Math.round(posicaoKhan[0] * larguraImagem);
            int y = yImagem + (int) Math.round(posicaoKhan[1] * alturaImagem);
            g.drawImage(imagemKhan, x - 26, y - 26, 52, 52, null);
        }

        g.dispose();
    }

    /** Posicoes editaveis associadas ao ID de cada parada, nunca a ordem do sorteio. */
    private void desenharPecasBonus(Graphics2D g) {
        for (br.herdeiros.model.ResumoConselheiro local : partida.consultarConselheiros()) {
            String arquivo = local.getImagemBonus();
            if (arquivo == null) continue;
            BufferedImage carta = cacheBonus.get(arquivo);
            if (carta == null) {
                try {
                    carta = ImageIO.read(new File("assets/cartas_bonus/" + arquivo));
                    if (carta == null) continue;
                    cacheBonus.put(arquivo, carta);
                } catch (IOException erro) { continue; }
            }
            LayoutAreas.Area area = areasCartas.consultar("bonus." + local.getIdParada());
            if (area == null) {
                MapaDados.Ponto ponto = mapa.getPonto(local.getIdParada());
                if (ponto == null) continue;
                area = new LayoutAreas.Area(ponto.getX(), Math.max(0.035, ponto.getY() - 0.05), 0.052, 0.052);
            }
            int cx = xImagem + (int) Math.round(area.x * larguraImagem);
            int cy = yImagem + (int) Math.round(area.y * alturaImagem);
            int wMax = Math.max(1, (int) Math.round(area.largura * larguraImagem));
            int hMax = Math.max(1, (int) Math.round(area.altura * alturaImagem));
            double escala = Math.min((double) wMax / carta.getWidth(), (double) hMax / carta.getHeight());
            int w = Math.max(1, (int) Math.round(carta.getWidth() * escala));
            int h = Math.max(1, (int) Math.round(carta.getHeight() * escala));
            g.drawImage(carta, cx - w / 2, cy - h / 2, w, h, null);
        }
        if (partida.isKarakorumEspecial()) {
            LayoutAreas.Area area = areasCartas.consultar("karakorum.especial");
            if (area == null) area = new LayoutAreas.Area(0.495, 0.38, 0.09, 0.09);
            if (area != null) {
                try {
                    BufferedImage carta = cacheBonus.get("cb_karakorum.jpg");
                    if (carta == null) {
                        carta = ImageIO.read(new File("assets/cartas_bonus/cb_karakorum.jpg"));
                        cacheBonus.put("cb_karakorum.jpg", carta);
                    }
                    int x = xImagem + (int) Math.round((area.x - area.largura / 2) * larguraImagem);
                    int y = yImagem + (int) Math.round((area.y - area.altura / 2) * alturaImagem);
                    g.drawImage(carta, x, y, (int) Math.round(area.largura * larguraImagem),
                            (int) Math.round(area.altura * alturaImagem), null);
                } catch (IOException erro) { /* Imagem opcional ausente. */ }
            }
        }
    }

    /** Indica os destinos realmente selecionáveis na ação obrigatória do Khan. */
    private void desenharDestinosDisponiveisKhan(Graphics2D g) {
        if (!partida.deveMoverKhanJogadorAtual() || partida.isPartidaEncerrada()) {
            return;
        }
        DestinoKhan atual = partida.consultarDestinoKhan();
        Graphics2D destaque = (Graphics2D) g.create();
        destaque.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        destaque.setStroke(new BasicStroke(3.0f));
        for (DestinoKhan destino : DestinoKhan.values()) {
            if (destino == atual) {
                continue;
            }
            double[] posicao = posicaoDestinoKhan(destino);
            double raio = raiosMarcadores.consultar("khan." + destino.name(), Math.sqrt(0.0030));
            int x = xImagem + (int) Math.round(posicao[0] * larguraImagem);
            int y = yImagem + (int) Math.round(posicao[1] * alturaImagem);
            int rx = Math.max(7, (int) Math.round(raio * larguraImagem));
            int ry = Math.max(7, (int) Math.round(raio * alturaImagem));
            destaque.setColor(new Color(84, 164, 255, 80));
            destaque.fillOval(x - rx, y - ry, rx * 2, ry * 2);
            destaque.setColor(new Color(40, 126, 255, 235));
            destaque.drawOval(x - rx, y - ry, rx * 2, ry * 2);
        }
        destaque.dispose();
    }

    private void desenharYurts(Graphics2D g) {
        for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
            java.util.List<br.herdeiros.model.Cor> donos = partida.consultarDonosYurtNaParada(ponto.getId());
            for (int i = 0; i < donos.size(); i++) {
                int x = xImagem + (int) Math.round(ponto.getX() * larguraImagem);
                int y = yImagem + (int) Math.round(ponto.getY() * alturaImagem);
                int deslocamento = (i == 0) ? -10 : 10;
                desenharYurt(g, x + deslocamento, y + 12, CoresJogadores.obter(donos.get(i)));
            }
        }
    }

    private void desenharYurt(Graphics2D g, int x, int y, Color cor) {
        Graphics2D tenda = (Graphics2D) g.create();
        tenda.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] xs = new int[] {x - 9, x, x + 9};
        int[] ys = new int[] {y + 9, y - 8, y + 9};
        tenda.setColor(new Color(30, 24, 17, 170));
        tenda.fillOval(x - 10, y + 6, 20, 6);
        tenda.setColor(cor);
        tenda.fillPolygon(xs, ys, 3);
        tenda.setColor(Color.WHITE);
        tenda.setStroke(new BasicStroke(1.8f));
        tenda.drawPolygon(xs, ys, 3);
        tenda.drawLine(x, y - 8, x, y + 12);
        tenda.dispose();
    }

    /** Contadores pequenos sobre os proprios icones, sem caixas cobrindo as paradas. */
    private void desenharContadores(Graphics2D g) {
        Graphics2D contadores = (Graphics2D) g.create();
        contadores.translate(xImagem, yImagem);
        contadores.scale(larguraImagem / 900.0, alturaImagem / 900.0);
        contadores.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (int indice = 0; indice < icones.getTributos().size(); indice++) {
            IconesTabuleiro.IconeTributo icone = icones.getTributos().get(indice);
            int quantidade = partida.consultarQuantidadeTributoIcone(indice);
            int x = (int) Math.round(icone.getX() * 900);
            int y = (int) Math.round(icone.getY() * 900);
            desenharNumero(contadores, x, y, quantidade, quantidade > 0);
        }

        List<ResumoCidade> abertas = partida.consultarCidadesDisponiveis();
        for (IconesTabuleiro.IconeCidade icone : icones.getCidades()) {
            for (ResumoCidade cidade : abertas) {
                if (mesmoNomeCidade(cidade.getNome(), icone.getNome())) {
                    int x = (int) Math.round(icone.getX() * 900);
                    int y = (int) Math.round(icone.getY() * 900);
                    // Cada ficha e identificada pelo tipo e fica visivel antes do saque.
                    List<br.herdeiros.model.TipoTesouro> fichas =
                            partida.consultarTesourosCidade(cidade.getNome());
                    for (int indice = 0; indice < fichas.size(); indice++) {
                        int posicaoX = x - 20 + (indice % 2) * 38;
                        int posicaoY = y - 20 + (indice / 2) * 38;
                        desenharFichaTesouro(contadores, posicaoX, posicaoY, fichas.get(indice));
                    }
                }
            }
        }
        // Yurt permanente do conquistador da cidade: influencia regional, nao movimento.
        for (IconesTabuleiro.IconeCidade icone : icones.getCidades()) {
            br.herdeiros.model.Cor conquistador = partida.consultarConquistadorCidade(icone.getNome());
            if (conquistador != null) {
                int x = (int) Math.round(icone.getX() * 900);
                int y = (int) Math.round(icone.getY() * 900);
                desenharYurt(contadores, x - 21, y + 18,
                        CoresJogadores.obter(conquistador));
            }
        }
        contadores.dispose();
    }



    /** Mercado desenhado nas quatro casas marcadas no editor, nunca em XY fixo. */
    private void desenharMercadoMelhorias(Graphics2D g) {
        Graphics2D mercado = (Graphics2D) g.create();
        mercado.translate(xImagem, yImagem);
        mercado.scale(larguraImagem / 900.0, alturaImagem / 900.0);
        mercado.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        List<ResumoMelhoria> lista = partida.consultarMercadoMelhorias();
        int largura = 76;
        int altura = 40;
        for (int i = 0; i < lista.size(); i++) {
            Posicao ponto = layoutMarcadores.consultar("mercado." + i);
            if (ponto == null) {
                continue;
            }
            LayoutAreas.Area area = areasCartas.consultar("mercado." + i);
            int larguraCarta = area == null ? largura : (int) Math.round(area.largura * 900);
            int alturaCarta = area == null ? altura : (int) Math.round(area.altura * 900);
            int x = area == null ? (int) Math.round(ponto.getX() * 900) - larguraCarta / 2
                    : (int) Math.round(area.x * 900) - larguraCarta / 2;
            int y = area == null ? (int) Math.round(ponto.getY() * 900) - alturaCarta / 2
                    : (int) Math.round(area.y * 900) - alturaCarta / 2;
            mercado.setColor(new Color(52, 32, 24, 150));
            mercado.fillRoundRect(x + 2, y + 3, larguraCarta, alturaCarta, 8, 8);
            mercado.setColor(new Color(246, 236, 214));
            mercado.fillRoundRect(x, y, larguraCarta, alturaCarta, 8, 8);
            mercado.setColor(new Color(103, 75, 48));
            mercado.drawRoundRect(x, y, larguraCarta, alturaCarta, 8, 8);
            ResumoMelhoria melhoria = lista.get(i);
            if (melhoria != null) {
                BufferedImage imagemMelhoria = carregarImagemMelhoria(melhoria.getCodigo());
                if (imagemMelhoria != null) {
                    mercado.drawImage(imagemMelhoria, x + 2, y + 2,
                            larguraCarta - 4, alturaCarta - 4, null);
                } else {
                    mercado.setColor(Color.DARK_GRAY);
                    mercado.setFont(new Font("SansSerif", Font.BOLD, 11));
                    mercado.drawString(melhoria.getCodigo(), x + 9, y + 24);
                }
            } else {
                mercado.setColor(new Color(125, 103, 82));
                mercado.setFont(new Font("SansSerif", Font.BOLD, 10));
                mercado.drawString("vazio", x + 17, y + 24);
            }
        }
        mercado.dispose();
    }

    /** Identifica carta pelo mesmo retangulo usado no desenho, inclusive apos redimensionar. */
    public int identificarMelhoriaMercadoClicada(int xTela, int yTela) {
        double[] posicao = converterClique(xTela, yTela);
        if (posicao == null) {
            return -1;
        }
        java.util.List<ResumoMelhoria> lista = partida.consultarMercadoMelhorias();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i) == null) {
                continue;
            }
            LayoutAreas.Area area = areasCartas.consultar("mercado." + i);
            if (area == null) {
                Posicao centro = layoutMarcadores.consultar("mercado." + i);
                if (centro == null) {
                    continue;
                }
                area = new LayoutAreas.Area(centro.getX(), centro.getY(), 76.0 / 900, 40.0 / 900);
            }
            if (area.contem(posicao[0], posicao[1])) {
                return i;
            }
        }
        return -1;
    }

    /** Distribui todos os jogadores empatados dentro da mesma casa da trilha. */
    private void desenharMarcadoresPontuacao(Graphics2D g) {
        Graphics2D placar = (Graphics2D) g.create();
        placar.translate(xImagem, yImagem);
        placar.scale(larguraImagem / 900.0, alturaImagem / 900.0);
        placar.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int totalJogadores = partida.getQuantidadeJogadores();
        int casaFinal = partida.consultarCasaFinal();
        if (casaFinal >= 0) {
            Posicao finalDaTrilha = layoutMarcadores.consultar("pontos." + casaFinal);
            if (finalDaTrilha != null) {
                desenharCasaFinal(placar,
                        (int) Math.round(finalDaTrilha.getX() * 900),
                        (int) Math.round(finalDaTrilha.getY() * 900),
                        (int) Math.round(raiosMarcadores.consultar("pontos." + casaFinal, 16.0 / 900) * 900));
            }
        }

        java.util.HashMap<Integer, java.util.List<Integer>> jogadoresPorCasa =
                new java.util.HashMap<Integer, java.util.List<Integer>>();
        for (int i = 0; i < totalJogadores; i++) {
            int pontos = partida.consultarVotosJogador(i);
            java.util.List<Integer> jogadores = jogadoresPorCasa.get(pontos);
            if (jogadores == null) {
                jogadores = new java.util.ArrayList<Integer>();
                jogadoresPorCasa.put(pontos, jogadores);
            }
            jogadores.add(i);
        }

        // A ficha neutra Khan compartilha a mesma trilha e determina o fim.
        int pontosKhan = partida.consultarPontuacaoKhan();
        if (pontosKhan > casaFinal) {
            pontosKhan = casaFinal;
        }
        java.util.List<Integer> mesmaCasa = jogadoresPorCasa.get(pontosKhan);
        if (mesmaCasa == null) {
            mesmaCasa = new java.util.ArrayList<Integer>();
            jogadoresPorCasa.put(pontosKhan, mesmaCasa);
        }
        mesmaCasa.add(-1);

        for (java.util.Map.Entry<Integer, java.util.List<Integer>> entrada
                : jogadoresPorCasa.entrySet()) {
            Posicao ponto = layoutMarcadores.consultar("pontos." + entrada.getKey());
            if (ponto == null) {
                continue;
            }
            int x = (int) Math.round(ponto.getX() * 900);
            int y = (int) Math.round(ponto.getY() * 900);
            java.util.List<Integer> jogadores = entrada.getValue();
            int quantidade = jogadores.size();
            int raioCasa = (int) Math.round(raiosMarcadores.consultar(
                    "pontos." + entrada.getKey(), 16.0 / 900) * 900);
            int raioPeca = Math.max(3, Math.min(quantidade == 1 ? 8 : 6, raioCasa / 2));
            int deslocamento = quantidade <= 2 ? Math.max(2, raioCasa / 3)
                    : Math.max(3, raioCasa / 2);
            for (int i = 0; i < quantidade; i++) {
                int dx = 0;
                int dy = 0;
                if (quantidade > 1) {
                    double angulo = 2.0 * Math.PI * i / quantidade - Math.PI / 2.0;
                    dx = (int) Math.round(Math.cos(angulo) * deslocamento);
                    dy = (int) Math.round(Math.sin(angulo) * deslocamento);
                }
                int indiceJogador = jogadores.get(i);
                if (indiceJogador == -1) {
                    desenharMarcadorPlacar(placar, x + dx, y + dy, raioPeca,
                            "KH", new Color(65, 45, 28));
                } else {
                    desenharMarcadorPlacar(placar, x + dx, y + dy, raioPeca,
                            String.valueOf(indiceJogador + 1),
                            CoresJogadores.obter(partida.getCorJogador(indiceJogador)));
                }
            }
        }
        placar.dispose();
    }

    private void desenharMarcadorPlacar(Graphics2D g, int x, int y, int raio,
                                        String texto, Color cor) {
        g.setColor(new Color(20, 16, 12, 160));
        g.fillOval(x - raio + 1, y - raio + 2, raio * 2, raio * 2);
        g.setColor(cor);
        g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(x - raio, y - raio, raio * 2, raio * 2);
        g.setFont(new Font("SansSerif", Font.BOLD, texto.equals("KH") ? 6 : 8));
        int largura = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, x - largura / 2, y + 3);
    }

    /** Circulo branco na casa de encerramento, definida pelo numero de jogadores. */
    private void desenharCasaFinal(Graphics2D g, int x, int y, int raio) {
        Graphics2D marca = (Graphics2D) g.create();
        marca.setColor(Color.WHITE);
        marca.fillOval(x - raio, y - raio, raio * 2, raio * 2);
        marca.setColor(new Color(48, 39, 62));
        marca.setStroke(new BasicStroke(2f));
        marca.drawOval(x - raio, y - raio, raio * 2, raio * 2);
        marca.setFont(new Font("SansSerif", Font.BOLD, 9));
        marca.drawString("FIM", x - 11, y - 4);
        marca.dispose();
    }

    private BufferedImage carregarImagemMelhoria(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        if (cacheMelhorias.containsKey(codigo)) {
            return cacheMelhorias.get(codigo);
        }
        try {
            BufferedImage imagemMelhoria = ImageIO.read(Paths.get("assets", "melhorias",
                    codigo + ".jpg").toFile());
            cacheMelhorias.put(codigo, imagemMelhoria);
            return imagemMelhoria;
        } catch (IOException erro) {
            cacheMelhorias.put(codigo, null);
            return null;
        }
    }

    public double[] posicaoDestinoKhan(DestinoKhan destino) {
        Posicao posicao = layoutMarcadores.consultar("khan." + destino.name());
        return posicao == null ? new double[] {destino.getX(), destino.getY()}
                : new double[] {posicao.getX(), posicao.getY()};
    }

    public boolean contemDestinoKhan(DestinoKhan destino, double x, double y) {
        double[] centro = posicaoDestinoKhan(destino);
        double dx = x - centro[0];
        double dy = y - centro[1];
        double raio = raiosMarcadores.consultar("khan." + destino.name(), Math.sqrt(0.0030));
        return dx * dx + dy * dy <= raio * raio;
    }

    private void desenharFichaTesouro(Graphics2D g, int x, int y,
                                     br.herdeiros.model.TipoTesouro tipo) {
        Graphics2D ficha = (Graphics2D) g.create();
        BufferedImage imagemTesouro = carregarImagemTesouro(tipo);
        if (imagemTesouro != null) {
            ficha.drawImage(imagemTesouro, x - 16, y - 16, 32, 32, null);
        } else {
            String sigla;
            switch (tipo) {
                case PELES: sigla = "ES"; break;
                case SEDA: sigla = "SE"; break;
                case PORCELANA: sigla = "PE"; break;
                case FERRO: sigla = "FE"; break;
                case GADO: sigla = "GA"; break;
                default: sigla = "?";
            }
            ficha.setColor(new Color(245, 231, 193));
            ficha.fillOval(x - 16, y - 16, 32, 32);
            ficha.setColor(new Color(76, 50, 31));
            ficha.setStroke(new BasicStroke(1.5f));
            ficha.drawOval(x - 16, y - 16, 32, 32);
            ficha.setFont(new Font("SansSerif", Font.BOLD, 9));
            ficha.drawString(sigla, x - ficha.getFontMetrics().stringWidth(sigla) / 2, y + 3);
        }
        ficha.dispose();
    }

    private BufferedImage carregarImagemTesouro(br.herdeiros.model.TipoTesouro tipo) {
        if (tipo == null) {
            return null;
        }
        if (cacheTesouros.containsKey(tipo)) {
            return cacheTesouros.get(tipo);
        }
        String arquivo;
        switch (tipo) {
            case GADO:
                arquivo = "gado.png";
                break;
            case SEDA:
                arquivo = "seda.png";
                break;
            case FERRO:
                arquivo = "ferro.png";
                break;
            case PORCELANA:
                arquivo = "peles.png";
                break;
            case PELES:
                arquivo = "especiarias.png";
                break;
            default:
                arquivo = null;
        }
        if (arquivo == null) {
            cacheTesouros.put(tipo, null);
            return null;
        }
        try {
            BufferedImage imagemTesouro = ImageIO.read(Paths.get("assets", "tesouros", arquivo).toFile());
            cacheTesouros.put(tipo, imagemTesouro);
            return imagemTesouro;
        } catch (IOException erro) {
            cacheTesouros.put(tipo, null);
            return null;
        }
    }

    private boolean mesmoNomeCidade(String primeiro, String segundo) {
        String nomeA = Normalizer.normalize(primeiro, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(java.util.Locale.ROOT);
        String nomeB = Normalizer.normalize(segundo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(java.util.Locale.ROOT);
        return nomeA.equals(nomeB);
    }

    private void desenharNumero(Graphics2D g, int x, int y, int numero, boolean disponivel) {
        int raio = 11;
        if (disponivel) {
            g.setColor(new Color(24, 83, 43, 220));
        } else {
            g.setColor(new Color(48, 43, 38, 190));
        }
        g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String texto = String.valueOf(numero);
        int largura = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, x - largura / 2, y + 5);
    }

    /** Encontra o icone real do tabuleiro; admite regioes na divisa. */
    public SelecaoTributo identificarTributoClicado(int xTela, int yTela) {
        double[] ponto = converterClique(xTela, yTela);
        if (ponto == null) {
            return null;
        }
        IconesTabuleiro.IconeTributo icone = icones.encontrarTributo(ponto[0], ponto[1]);
        if (icone == null) {
            return null;
        }
        return new SelecaoTributo(icones.getTributos().indexOf(icone),
                icone.getProvincias(), icone.getTipo());
    }

    /** Permite clicar diretamente em uma ficha de tesouro visivel na cidade. */
    public SelecaoTesouroCidade identificarFichaCidadeClicada(int xTela, int yTela) {
        double[] ponto = converterClique(xTela, yTela);
        if (ponto == null) {
            return null;
        }
        double px = ponto[0] * 900.0;
        double py = ponto[1] * 900.0;
        for (IconesTabuleiro.IconeCidade icone : icones.getCidades()) {
            List<br.herdeiros.model.TipoTesouro> fichas =
                    partida.consultarTesourosCidade(icone.getNome());
            int x = (int) Math.round(icone.getX() * 900);
            int y = (int) Math.round(icone.getY() * 900);
            for (int indice = 0; indice < fichas.size(); indice++) {
                int centroX = x - 20 + (indice % 2) * 38;
                int centroY = y - 20 + (indice / 2) * 38;
                double dx = px - centroX;
                double dy = py - centroY;
                if (dx * dx + dy * dy <= 16 * 16) {
                    return new SelecaoTesouroCidade(icone.getNome(), fichas.get(indice));
                }
            }
        }
        return null;
    }

    public static final class SelecaoTesouroCidade {
        private final String nomeCidade;
        private final br.herdeiros.model.TipoTesouro tipo;

        private SelecaoTesouroCidade(String nomeCidade, br.herdeiros.model.TipoTesouro tipo) {
            this.nomeCidade = nomeCidade;
            this.tipo = tipo;
        }

        public String getNomeCidade() { return nomeCidade; }
        public br.herdeiros.model.TipoTesouro getTipo() { return tipo; }
    }

    public String identificarCidadeClicada(int xTela, int yTela) {
        double[] ponto = converterClique(xTela, yTela);
        if (ponto == null) {
            return null;
        }
        IconesTabuleiro.IconeCidade icone = icones.encontrarCidade(ponto[0], ponto[1]);
        if (icone == null) {
            return null;
        }
        return icone.getNome();
    }

    public static final class SelecaoTributo {
        private final int indice;
        private final Provincia[] provincias;
        private final String tipo;

        private SelecaoTributo(int indice, Provincia[] provincias, String tipo) {
            this.indice = indice;
            this.provincias = provincias.clone();
            this.tipo = tipo;
        }

        public int getIndice() {
            return indice;
        }

        public Provincia[] getProvincias() {
            return provincias.clone();
        }

        public Provincia getProvincia() {
            return provincias[0];
        }

        public String getTipo() {
            return tipo;
        }
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        repaint();
    }

    private int calcularDeslocamentoHorizontal(int indiceJogador, int idParada) {
        int quantidadeNaParada = 0;
        int posicaoNaParada = 0;
        for (int i = 0; i < partida.getQuantidadeJogadores(); i++) {
            if (partida.getParadaJogador(i) == idParada) {
                if (i < indiceJogador) {
                    posicaoNaParada++;
                }
                quantidadeNaParada++;
            }
        }
        return (int) Math.round((posicaoNaParada - (quantidadeNaParada - 1) / 2.0) * 23);
    }

    public int getLarguraImagem() {
        return larguraImagem;
    }

    public int getAlturaImagem() {
        return alturaImagem;
    }

    public double[] converterClique(int x, int y) {
        if (larguraImagem <= 0 || alturaImagem <= 0 || x < xImagem || y < yImagem
                || x > xImagem + larguraImagem || y > yImagem + alturaImagem) {
            return null;
        }
        return new double[] {(double) (x - xImagem) / larguraImagem,
                             (double) (y - yImagem) / alturaImagem};
    }
}
