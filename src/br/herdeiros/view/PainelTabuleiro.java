package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
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
import java.nio.file.Paths;
import java.util.List;
import java.text.Normalizer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** O JPanel é somente a superfície: imagens são desenhadas com Graphics2D.drawImage. */
public final class PainelTabuleiro extends JPanel implements ObservadorPartida {
    private static final long serialVersionUID = 1L;
    private final BufferedImage imagem;
    private final BufferedImage[] imagemPeoes;
    private final BufferedImage imagemKhan;
    private final MapaDados mapa;
    private final IconesTabuleiro icones;
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
        this.controlador = controlador;
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

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double escala = Math.min((double) getWidth() / imagem.getWidth(),
                                 (double) getHeight() / imagem.getHeight());
        larguraImagem = (int) Math.round(imagem.getWidth() * escala);
        alturaImagem = (int) Math.round(imagem.getHeight() * escala);
        xImagem = (getWidth() - larguraImagem) / 2;
        yImagem = (getHeight() - alturaImagem) / 2;
        g.drawImage(imagem, xImagem, yImagem, larguraImagem, alturaImagem, null);
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
        desenharContadores(g);
        DestinoKhan destinoKhan = partida.consultarDestinoKhan();
        if (destinoKhan != null) {
            int x = xImagem + (int) Math.round(destinoKhan.getX() * larguraImagem);
            int y = yImagem + (int) Math.round(destinoKhan.getY() * alturaImagem);
            g.drawImage(imagemKhan, x - 26, y - 26, 52, 52, null);
        }
        g.dispose();
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
