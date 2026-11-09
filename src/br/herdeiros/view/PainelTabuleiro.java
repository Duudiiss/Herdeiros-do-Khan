package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
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
    private final MapaDados mapa;
    private final IconesTabuleiro icones;
    private final LayoutMarcadores layoutMarcadores;
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
        this.controlador = controlador;
        this.cacheMelhorias = new HashMap<String, BufferedImage>();
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
            int id = partida.getParadaJogador(i);
            MapaDados.Ponto ponto = mapa.getPonto(id);
            if (ponto != null) {
                int x = xImagem + (int) Math.round(ponto.getX() * larguraImagem);
                int y = yImagem + (int) Math.round(ponto.getY() * alturaImagem);
                int deslocamento = (int) Math.round((i - (imagemPeoes.length - 1) / 2.0) * 23);
                g.drawImage(imagemPeoes[i], x + deslocamento - 16, y - 47, 32, 36, null);
            }
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
        desenharMarcadoresPontuacao(g);
        DestinoKhan destinoKhan = partida.consultarDestinoKhan();
        if (destinoKhan != null) {
            int x = xImagem + (int) Math.round(destinoKhan.getX() * larguraImagem);
            int y = yImagem + (int) Math.round(destinoKhan.getY() * alturaImagem);
            g.drawImage(imagemKhan, x - 26, y - 26, 52, 52, null);
        }

        g.dispose();
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

        for (IconesTabuleiro.IconeTributo icone : icones.getTributos()) {
            int quantidade = 0;
            for (Provincia provincia : icone.getProvincias()) {
                quantidade += partida.consultarQuantidadeTributoProvincia(provincia, icone.getTipo());
            }
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
                    // A marca pequena da cidade identifica quantos tesouros ainda restam.
                    desenharNumero(contadores, x + 27, y + 23, cidade.getTesourosRestantes(), true);
                }
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
            int x = (int) Math.round(ponto.getX() * 900) - largura / 2;
            int y = (int) Math.round(ponto.getY() * 900) - altura / 2;
            mercado.setColor(new Color(52, 32, 24, 150));
            mercado.fillRoundRect(x + 2, y + 3, largura, altura, 8, 8);
            mercado.setColor(new Color(246, 236, 214));
            mercado.fillRoundRect(x, y, largura, altura, 8, 8);
            mercado.setColor(new Color(103, 75, 48));
            mercado.drawRoundRect(x, y, largura, altura, 8, 8);
            ResumoMelhoria melhoria = lista.get(i);
            if (melhoria != null) {
                BufferedImage imagemMelhoria = carregarImagemMelhoria(melhoria.getCodigo());
                if (imagemMelhoria != null) {
                    mercado.drawImage(imagemMelhoria, x + 2, y + 2,
                            largura - 4, altura - 4, null);
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
                        (int) Math.round(finalDaTrilha.getY() * 900));
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
            int raioPeca = quantidade == 1 ? 8 : 6;
            int deslocamento = quantidade <= 2 ? 6 : 8;
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
    private void desenharCasaFinal(Graphics2D g, int x, int y) {
        Graphics2D marca = (Graphics2D) g.create();
        marca.setColor(Color.WHITE);
        marca.fillOval(x - 16, y - 16, 32, 32);
        marca.setColor(new Color(48, 39, 62));
        marca.setStroke(new BasicStroke(2f));
        marca.drawOval(x - 16, y - 16, 32, 32);
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
        return new SelecaoTributo(icone.getProvincias(), icone.getTipo());
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
        private final Provincia[] provincias;
        private final String tipo;

        private SelecaoTributo(Provincia[] provincias, String tipo) {
            this.provincias = provincias.clone();
            this.tipo = tipo;
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
