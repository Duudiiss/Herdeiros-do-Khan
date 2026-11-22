package br.herdeiros.view;

import br.herdeiros.model.Cor;
import br.herdeiros.layout.LayoutAreas;
import br.herdeiros.layout.LayoutMarcadores;
import br.herdeiros.layout.LayoutMarcadores.Posicao;
import br.herdeiros.model.ResumoAtivacao;
import br.herdeiros.model.ResumoMelhoria;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Usa JPanel apenas como superficie de pintura. A imagem da cartela e sempre
 * apresentada por Graphics2D.drawImage(), conforme o enunciado.
 */
public final class PainelCartela extends JPanel {

    private static final long serialVersionUID = 1L;
    private final BufferedImage[] imagens;
    private IntConsumer aoSelecionarColuna;
    private int indiceCartela;
    private ResumoAtivacao ativacao;
    private int votos;
    private Cor corJogador;
    private List<ResumoMelhoria> melhoriasAdquiridas;
    private final Map<String, BufferedImage> cacheMelhorias;
    private final Map<Cor, BufferedImage> cacheTokensAtivacao;
    private final LayoutMarcadores[] layoutsCartelas;
    private final LayoutAreas[] areasCartelas;

    public PainelCartela() throws IOException {
        imagens = new BufferedImage[5];
        for (int i = 0; i < imagens.length; i++) {
            String caminho = "assets/cartelas/cartela" + (i + 1) + ".jpg";
            imagens[i] = ImageIO.read(new File(caminho));
            if (imagens[i] == null) {
                throw new IOException("Imagem de cartela invalida: " + caminho);
            }
        }
        indiceCartela = 0;
        melhoriasAdquiridas = new ArrayList<ResumoMelhoria>();
        cacheMelhorias = new HashMap<String, BufferedImage>();
        cacheTokensAtivacao = new HashMap<Cor, BufferedImage>();
        layoutsCartelas = new LayoutMarcadores[5];
        areasCartelas = new LayoutAreas[5];
        for (int i = 0; i < layoutsCartelas.length; i++) {
            layoutsCartelas[i] = new LayoutMarcadores(LayoutMarcadores.caminhoCartela(i + 1));
            areasCartelas[i] = new LayoutAreas(LayoutAreas.caminhoCartela(i + 1));
        }
        
        setPreferredSize(new Dimension(1180, 730));
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                selecionarColunaPeloClique(evento.getX(), evento.getY());
            }
        });
    }

    public void mostrarCartela(int indice, ResumoAtivacao novaAtivacao, int novosVotos,
                               Cor novaCor, List<ResumoMelhoria> novasMelhorias) {
        if (indice < 0 || indice >= imagens.length) {
            throw new IllegalArgumentException("Cartela inexistente");
        }
        indiceCartela = indice;
        ativacao = novaAtivacao;
        votos = novosVotos;
        corJogador = novaCor;
        if (novasMelhorias == null) {
            melhoriasAdquiridas = new ArrayList<ResumoMelhoria>();
        } else {
            melhoriasAdquiridas = new ArrayList<ResumoMelhoria>(novasMelhorias);
        }
        repaint();
    }

    public void mostrarColunasIndisponiveis(ResumoAtivacao estado) {
        this.ativacao = estado;
        repaint();
    }

    public void definirAcaoDeClique(IntConsumer acao) {
        aoSelecionarColuna = acao;
    }

    private void selecionarColunaPeloClique(int cliqueX, int cliqueY) {
        if (ativacao == null || aoSelecionarColuna == null) {
            return;
        }
        BufferedImage imagem = imagens[indiceCartela];
        double escala = Math.min((double) getWidth() / imagem.getWidth(),
                (double) getHeight() / imagem.getHeight());
        double origemX = (getWidth() - imagem.getWidth() * escala) / 2.0;
        double origemY = (getHeight() - imagem.getHeight() * escala) / 2.0;
        double normalX = (cliqueX - origemX) / (imagem.getWidth() * escala);
        double normalY = (cliqueY - origemY) / (imagem.getHeight() * escala);
        for (int coluna = 0; coluna < 4; coluna++) {
            LayoutAreas.Area area = areasCartelas[indiceCartela].consultar("ativacao." + coluna);
            Posicao ponto = layoutsCartelas[indiceCartela].consultar("ativacao." + coluna);
            if (ponto == null && area == null) {
                continue;
            }
            double cx = area == null ? ponto.getX() : area.x;
            double largura = area == null ? 220.0 / imagem.getWidth() : area.largura;
            // A coluna inteira, inclusive as cartas desenhadas acima da base,
            // funciona como area de ativacao. O editor define a faixa horizontal.
            if (Math.abs(normalX - cx) <= largura / 2
                    && normalY >= 0.08 && normalY <= 0.97) {
                if (ativacao.podeAtivar(coluna)) {
                    aoSelecionarColuna.accept(coluna);
                }
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        EstiloImperial.pintarFundoImperial(graficos, this, null);
        BufferedImage imagem = imagens[indiceCartela];
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        double escalaX = (double) getWidth() / imagem.getWidth();
        double escalaY = (double) getHeight() / imagem.getHeight();
        double escala = Math.min(escalaX, escalaY);
        int largura = (int) Math.round(imagem.getWidth() * escala);
        int altura = (int) Math.round(imagem.getHeight() * escala);
        int x = (getWidth() - largura) / 2;
        int y = (getHeight() - altura) / 2;

        g.drawImage(imagem, x, y, largura, altura, null);
        desenharConteudoSobreCartela(g, x, y, escala);
        g.dispose();
    }

    private void desenharConteudoSobreCartela(Graphics2D g, int x, int y, double escala) {
        if (corJogador == null) {
            return;
        }

        if (ativacao != null && ativacao.getColunaAtiva() >= 0) {
            destacarColunaAtiva(g, x, y, escala);
        }

        desenharMelhoriasAdquiridas(g, x, y, escala);
        desenharColunasBloqueadas(g, x, y, escala);
    }

    /**
     * Uma coluna que ja foi utilizada recebe uma ficha fisica da cor
     * do jogador sobre a base da acao. O editor unificado define o centro
     * da area de ativacao, portanto o token acompanha cada cartela.
     */
    private void desenharColunasBloqueadas(Graphics2D g, int x, int y, double escala) {
        if (ativacao == null || corJogador == null) {
            return;
        }
        BufferedImage imagem = imagens[indiceCartela];
        BufferedImage token = carregarTokenAtivacao(corJogador);
        for (int coluna = 0; coluna < 4; coluna++) {
            if (!ativacao.estaOcupada(coluna)) {
                continue;
            }
            LayoutAreas.Area area = areasCartelas[indiceCartela].consultar("ativacao." + coluna);
            Posicao ponto = layoutsCartelas[indiceCartela].consultar("ativacao." + coluna);
            if (area == null && ponto == null) {
                continue;
            }
            double centroX = area == null ? ponto.getX() : area.x;
            double centroY = area == null ? ponto.getY() : area.y;
            int cx = x + (int) Math.round(centroX * imagem.getWidth() * escala);
            int cy = y + (int) Math.round(centroY * imagem.getHeight() * escala);
            int diametro = Math.max(18, (int) Math.round(75 * escala));
            Graphics2D marcador = (Graphics2D) g.create();
            if (token != null) {
                marcador.drawImage(token, cx - diametro / 2, cy - diametro / 2,
                        diametro, diametro, null);
            } else {
                // Cor laranja e outras cores nao tem imagem fornecida:
                // desenhar um marcador circular da propria cor.
                marcador.setColor(CoresJogadores.obter(corJogador));
                marcador.fillOval(cx - diametro / 2, cy - diametro / 2,
                        diametro, diametro);
                marcador.setColor(Color.WHITE);
                marcador.setStroke(new BasicStroke(2f));
                marcador.drawOval(cx - diametro / 2, cy - diametro / 2,
                        diametro, diametro);
            }
            marcador.dispose();
        }
    }

    private BufferedImage carregarTokenAtivacao(Cor cor) {
        if (cacheTokensAtivacao.containsKey(cor)) {
            return cacheTokensAtivacao.get(cor);
        }
        String arquivo;
        switch (cor) {
            case AZUL: arquivo = "azul.png"; break;
            case AMARELO: arquivo = "amarelo.png"; break;
            case PRETO: arquivo = "preto.png"; break;
            case VERMELHO: arquivo = "vermelho.png"; break;
            case VERDE: arquivo = "verde.png"; break;
            default: arquivo = null;
        }
        BufferedImage imagemToken = null;
        if (arquivo != null) {
            try {
                imagemToken = ImageIO.read(new File("assets/ativacoes/" + arquivo));
            } catch (IOException erro) {
                imagemToken = null;
            }
        }
        cacheTokensAtivacao.put(cor, imagemToken);
        return imagemToken;
    }

    private void destacarColunaAtiva(Graphics2D g, int x, int y, double escala) {
        int coluna = ativacao.getColunaAtiva();
        Posicao ponto = layoutsCartelas[indiceCartela].consultar("ativacao." + coluna);
        if (ponto == null) {
            return;
        }
        BufferedImage imagem = imagens[indiceCartela];
        int centroX = x + (int) Math.round(ponto.getX() * imagem.getWidth() * escala);
        int centroY = y + (int) Math.round(ponto.getY() * imagem.getHeight() * escala);
        LayoutAreas.Area area = areasCartelas[indiceCartela].consultar("ativacao." + coluna);
        if (area != null) {
            centroX = x + (int) Math.round(area.x * imagem.getWidth() * escala);
            centroY = y + (int) Math.round(area.y * imagem.getHeight() * escala);
        }
        int largura = Math.max(8, (int) Math.round(
                (area == null ? 220 : area.largura * imagem.getWidth()) * escala));
        int altura = Math.max(8, (int) Math.round(
                (area == null ? 105 : area.altura * imagem.getHeight()) * escala));
        Graphics2D destaque = (Graphics2D) g.create();
        destaque.setColor(new Color(255, 220, 120, 55));
        destaque.fillRoundRect(centroX - largura / 2, centroY - altura / 2,
                largura, altura, 10, 10);
        destaque.setColor(CoresJogadores.obter(corJogador));
        destaque.setStroke(new BasicStroke(Math.max(2f, (float) (3 * escala))));
        destaque.drawRoundRect(centroX - largura / 2, centroY - altura / 2,
                largura, altura, 10, 10);
        destaque.dispose();
    }

    private void desenharMelhoriasAdquiridas(Graphics2D g, int x, int y, double escala) {
        if (melhoriasAdquiridas == null || melhoriasAdquiridas.isEmpty()) {
            return;
        }
        int[] ocupacaoPorColuna = new int[] {0, 0, 0, 0};
        BufferedImage imagem = imagens[indiceCartela];
        int larguraCarta = Math.max(10, (int) Math.round(218 * escala));
        int alturaCarta = Math.max(6, (int) Math.round(114 * escala));
        for (ResumoMelhoria melhoria : melhoriasAdquiridas) {
            if (melhoria == null) {
                continue;
            }
            int coluna = melhoria.getColuna();
            if (coluna < 0 || coluna >= 4) {
                continue;
            }
            int linha = ocupacaoPorColuna[coluna];
            ocupacaoPorColuna[coluna] = linha + 1;
            Posicao ponto = layoutsCartelas[indiceCartela].consultar(
                    "melhoria." + coluna + "." + linha);
            if (ponto == null) {
                continue;
            }
            LayoutAreas.Area area = areasCartelas[indiceCartela].consultar(
                    "melhoria." + coluna + "." + linha);
            int larguraAtual = area == null ? larguraCarta
                    : Math.max(4, (int) Math.round(area.largura * imagem.getWidth() * escala));
            int alturaAtual = area == null ? alturaCarta
                    : Math.max(4, (int) Math.round(area.altura * imagem.getHeight() * escala));
            int centroX = x + (int) Math.round((area == null ? ponto.getX() : area.x) * imagem.getWidth() * escala);
            int centroY = y + (int) Math.round((area == null ? ponto.getY() : area.y) * imagem.getHeight() * escala);
            desenharCartaMelhoria(g, melhoria, centroX - larguraAtual / 2,
                    centroY - alturaAtual / 2, larguraAtual, alturaAtual);
        }
    }

    private void desenharCartaMelhoria(Graphics2D g, ResumoMelhoria melhoria,
                                       int x, int y, int largura, int altura) {
        BufferedImage imagem = carregarImagemMelhoria(melhoria.getCodigo());

        g.setColor(new Color(40, 31, 24, 150));
        g.fillRoundRect(x + 2, y + 3, largura, altura, 10, 10);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(x, y, largura, altura, 10, 10);

        if (imagem != null) {
            g.drawImage(imagem, x + 2, y + 2, largura - 4, altura - 4, null);
        } else {
            g.setColor(new Color(240, 232, 212));
            g.fillRoundRect(x + 2, y + 2, largura - 4, altura - 4, 10, 10);
            g.setColor(Color.DARK_GRAY);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            g.drawString(melhoria.getCodigo(), x + 8, y + altura / 2 + 4);
        }
    }

    private BufferedImage carregarImagemMelhoria(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        if (cacheMelhorias.containsKey(codigo)) {
            return cacheMelhorias.get(codigo);
        }
        try {
            BufferedImage imagem = ImageIO.read(new File("assets/melhorias/" + codigo + ".jpg"));
            cacheMelhorias.put(codigo, imagem);
            return imagem;
        } catch (IOException erro) {
            cacheMelhorias.put(codigo, null);
            return null;
        }
    }
}
