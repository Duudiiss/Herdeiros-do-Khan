package br.herdeiros.editor;

import br.herdeiros.layout.LayoutAreas;
import br.herdeiros.layout.LayoutMarcadores;
import br.herdeiros.layout.LayoutRaios;
import br.herdeiros.layout.KhanRecursosLayout;
import br.herdeiros.mapa.IconesTabuleiro;
import br.herdeiros.model.DestinoKhan;
import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/** Um unico editor para posicoes, hitboxes ovais e cartas retangulares. */
public final class JanelaEditorTabuleiroUnificado extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JComboBox<String> imagemSelecionada = new JComboBox<>(new String[] {
        "Tabuleiro", "Altani", "Jochi", "Chagatai", "Tolui", "Ogedei"
    });
    private final JComboBox<String> ferramenta = new JComboBox<>(new String[] {
        "Paradas (circulo / elipse)", "Cartas (retangulo)",
        "Destaques de acao (retangulo)", "Icones de recursos / cidades (circulo)",
        "Destinos do Khan (circulo)", "Marcadores de pontos (circulo)",
        "Recursos controlados pelo Khan", "Pecas bonus por parada (retangulo)"
    });
    private final JComboBox<String> elemento = new JComboBox<>();
    private final JSpinner largura = new JSpinner(new SpinnerNumberModel(4.0, 0.4, 35.0, 0.2));
    private final JSpinner altura = new JSpinner(new SpinnerNumberModel(4.0, 0.4, 35.0, 0.2));
    private final JLabel mensagem = new JLabel("Selecione um elemento. Clique para posicionar; arraste para ajustar.");
    private final TelaEdicao tela = new TelaEdicao();
    private BufferedImage imagem;
    private MapaDados mapa;
    private LayoutAreas areas;
    private LayoutMarcadores marcadores;
    private LayoutRaios raios;
    private IconesTabuleiro icones;
    private KhanRecursosLayout recursosKhan;
    private boolean alterado;
    private boolean preenchendo;
    private int imagemAnterior;

    public JanelaEditorTabuleiroUnificado() throws IOException {
        super("Herdeiros de Khan - Editor unificado de tabuleiro e cartelas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1250, 920);
        setLocationByPlatform(true);
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
        topo.add(new JLabel("Imagem:"));
        topo.add(imagemSelecionada);
        topo.add(new JLabel("Editar:"));
        topo.add(ferramenta);
        topo.add(new JLabel("Elemento:"));
        topo.add(elemento);
        topo.add(new JLabel("Largura / raio X (%):"));
        topo.add(largura);
        topo.add(new JLabel("Altura / raio Y (%):"));
        topo.add(altura);
        JButton salvar = new JButton("Salvar todas as edicoes");
        topo.add(salvar);
        JButton conectar = new JButton("Conectar paradas");
        JButton desconectar = new JButton("Desconectar paradas");
        topo.add(conectar);
        topo.add(desconectar);
        JButton recursosDoKhan = new JButton("Associar recursos ao Khan");
        recursosDoKhan.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { associarRecursosKhan(); } });
        topo.add(recursosDoKhan);
        JButton acessoTributo = new JButton("Paradas que podem coletar tributo");
        acessoTributo.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evento) {
                editarParadasTributo();
            }
        });
        topo.add(acessoTributo);
        conectar.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { editarConexao(true); } });
        desconectar.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { editarConexao(false); } });
        add(topo, BorderLayout.NORTH);
        add(tela, BorderLayout.CENTER);
        add(mensagem, BorderLayout.SOUTH);
        imagemSelecionada.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { trocarImagem(); } });
        ferramenta.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { atualizarLista(); } });
        elemento.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { atualizarDimensoes(); } });
        largura.addChangeListener(new javax.swing.event.ChangeListener() { @Override public void stateChanged(javax.swing.event.ChangeEvent e) { dimensoesAlteradas(); } });
        altura.addChangeListener(new javax.swing.event.ChangeListener() { @Override public void stateChanged(javax.swing.event.ChangeEvent e) { dimensoesAlteradas(); } });
        salvar.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { salvar(); } });
        trocarImagem();
    }

    private boolean confirmarAlteracoes() {
        if (!alterado) {
            return true;
        }
        int escolha = JOptionPane.showConfirmDialog(this,
                "Salvar as alteracoes anteriores?", "Edicoes pendentes",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (escolha == JOptionPane.CANCEL_OPTION || escolha == JOptionPane.CLOSED_OPTION) {
            return false;
        }
        if (escolha == JOptionPane.YES_OPTION) {
            return salvar();
        }
        return true;
    }

    private void trocarImagem() {
        if (preenchendo) {
            return;
        }
        if (!confirmarAlteracoes()) {
            preenchendo = true;
            imagemSelecionada.setSelectedIndex(imagemAnterior);
            preenchendo = false;
            return;
        }
        int indice = imagemSelecionada.getSelectedIndex();
        try {
            imagem = ImageIO.read(new File(indice == 0
                    ? "assets/tabuleiro.jpg" : "assets/cartelas/cartela" + indice + ".jpg"));
            mapa = indice == 0 ? ArquivoMapa.carregar(Paths.get("assets/mapa_paradas.csv")) : null;
            areas = new LayoutAreas(indice == 0
                    ? LayoutAreas.caminhoTabuleiro() : LayoutAreas.caminhoCartela(indice));
            marcadores = new LayoutMarcadores(indice == 0
                    ? LayoutMarcadores.caminhoTabuleiro() : LayoutMarcadores.caminhoCartela(indice));
            raios = indice == 0 ? new LayoutRaios(LayoutRaios.caminhoTabuleiro()) : null;
            icones = indice == 0 ? IconesTabuleiro.carregar(Paths.get("assets/icones_tabuleiro.csv")) : null;
            recursosKhan = indice == 0 ? KhanRecursosLayout.carregar() : null;
            imagemAnterior = indice;
            if (indice != 0 && (ferramenta.getSelectedIndex() == 0
                    || ferramenta.getSelectedIndex() >= 3)) {
                ferramenta.setSelectedIndex(1);
            }
            alterado = false;
            atualizarLista();
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage());
        }
    }

    private void atualizarLista() {
        if (preenchendo || areas == null) {
            return;
        }
        preenchendo = true;
        int tipo = ferramenta.getSelectedIndex();
        if (imagemSelecionada.getSelectedIndex() != 0 && (tipo == 0 || tipo >= 3)) {
            ferramenta.setSelectedIndex(1);
            tipo = 1;
        }
        elemento.removeAllItems();
        if (tipo == 0 && mapa != null) {
            for (Integer id : mapa.getPontos().keySet()) {
                elemento.addItem("Parada " + id);
            }
        } else if (tipo == 1) {
            List<String> ids = imagemSelecionada.getSelectedIndex() == 0
                    ? LayoutMarcadores.chavesTabuleiro() : LayoutMarcadores.chavesCartela();
            for (String id : ids) {
                if (id.startsWith("mercado.") || id.startsWith("melhoria.")) {
                    elemento.addItem(id);
                }
            }
        } else if (tipo == 2) {
            for (int coluna = 0; coluna < 4; coluna++) {
                elemento.addItem("ativacao." + coluna);
            }
        } else if (tipo == 3 && icones != null) {
            for (int i = 0; i < icones.getTributos().size(); i++) {
                IconesTabuleiro.IconeTributo icone = icones.getTributos().get(i);
                elemento.addItem("tributo." + i + " (" + icone.getTipo() + ")");
            }
            for (int i = 0; i < icones.getCidades().size(); i++) {
                elemento.addItem("cidade." + i + " (" + icones.getCidades().get(i).getNome() + ")");
            }
        } else if (tipo == 4 || tipo == 6) {
            for (DestinoKhan destino : DestinoKhan.values()) {
                elemento.addItem("khan." + destino.name());
            }
        } else if (tipo == 7 && mapa != null) {
            // Cada retangulo e vinculado ao ID imutavel da parada correspondente.
            int[] locais = {0, 28, 29, 30, 31, 32, 33};
            for (int id : locais) {
                // O ponto 0 e Karakorum; 28 a 33 sao as seis paradas duplas.
                elemento.addItem("bonus." + id);
            }
            elemento.addItem("karakorum.especial");
        } else if (tipo == 5) {
            for (int i = 0; i <= 16; i++) {
                elemento.addItem("pontos." + i);
            }
        }
        preenchendo = false;
        atualizarDimensoes();
        tela.repaint();
    }

    private String chave() {
        return (String) elemento.getSelectedItem();
    }

    private int idParada() {
        String selecionado = chave();
        return selecionado == null ? -1 : Integer.parseInt(selecionado.substring("Parada ".length()));
    }

    private IconesTabuleiro.IconeTributo tributoSelecionado() {
        String id = chave();
        if (icones == null || id == null || !id.startsWith("tributo.")) {
            return null;
        }
        return icones.getTributos().get(Integer.parseInt(id.split("[. (]")[1]));
    }

    private IconesTabuleiro.IconeCidade cidadeSelecionada() {
        String id = chave();
        if (icones == null || id == null || !id.startsWith("cidade.")) {
            return null;
        }
        return icones.getCidades().get(Integer.parseInt(id.split("[. (]")[1]));
    }

    private String chaveMarcador() {
        return chave();
    }

    private LayoutAreas.Area areaSelecionada() {
        if (areas == null || chave() == null) {
            return null;
        }
        LayoutAreas.Area existente = areas.consultar(chave());
        if (existente != null) {
            return existente;
        }
        LayoutMarcadores.Posicao centro = marcadores.consultar(chave());
        if (centro == null && ferramenta.getSelectedIndex() == 7 && mapa != null) {
            if ("karakorum.especial".equals(chave())) return new LayoutAreas.Area(0.495, 0.38, 0.09, 0.09);
            try {
                int id = Integer.parseInt(chave().substring("bonus.".length()));
                MapaDados.Ponto ponto = mapa.getPonto(id);
                if (ponto != null) return new LayoutAreas.Area(ponto.getX(),
                        Math.max(0.035, ponto.getY() - 0.05), 0.052, 0.052);
            } catch (NumberFormatException erro) { return null; }
        }
        if (centro == null) {
            return null;
        }
        boolean tabuleiro = imagemSelecionada.getSelectedIndex() == 0;
        boolean destaque = ferramenta.getSelectedIndex() == 2;
        return new LayoutAreas.Area(centro.getX(), centro.getY(),
                destaque ? 220.0 / 1650 : (tabuleiro ? 76.0 / 900 : 218.0 / 1650),
                destaque ? 105.0 / 898 : (tabuleiro ? (ferramenta.getSelectedIndex() == 7 ? 0.052 : 40.0 / 900) : 114.0 / 898));
    }

    private void atualizarDimensoes() {
        if (preenchendo || chave() == null) {
            tela.repaint();
            return;
        }
        preenchendo = true;
        if (ferramenta.getSelectedIndex() == 0 && mapa != null) {
            MapaDados.Ponto ponto = mapa.getPonto(idParada());
            if (ponto != null) {
                largura.setValue(ponto.getRaio() * 100);
                altura.setValue(ponto.getRaioVertical() * 100);
            }
        } else if (ferramenta.getSelectedIndex() == 1 || ferramenta.getSelectedIndex() == 2 || ferramenta.getSelectedIndex() == 7) {
            LayoutAreas.Area area = areaSelecionada();
            if (area != null) {
                largura.setValue(area.largura * 100);
                altura.setValue(area.altura * 100);
            }
        } else if (ferramenta.getSelectedIndex() == 3) {
            IconesTabuleiro.IconeTributo tributo = tributoSelecionado();
            IconesTabuleiro.IconeCidade cidade = cidadeSelecionada();
            double raio = tributo != null ? tributo.getRaio()
                    : (cidade != null ? cidade.getRaio() : 0.02);
            largura.setValue(raio * 100);
            altura.setValue(raio * 100);
        } else if (ferramenta.getSelectedIndex() == 4 || ferramenta.getSelectedIndex() == 5) {
            double padrao = ferramenta.getSelectedIndex() == 4 ? Math.sqrt(0.0030) : 16.0 / 900;
            double raio = raios.consultar(chaveMarcador(), padrao);
            largura.setValue(raio * 100);
            altura.setValue(raio * 100);
        }
        preenchendo = false;
        tela.repaint();
    }

    private void dimensoesAlteradas() {
        if (preenchendo || chave() == null) {
            return;
        }
        double w = ((Number) largura.getValue()).doubleValue() / 100;
        double h = ((Number) altura.getValue()).doubleValue() / 100;
        try {
            if (ferramenta.getSelectedIndex() == 0 && mapa != null) {
                mapa.alterarElipse(idParada(), w, h);
            } else if (ferramenta.getSelectedIndex() == 1 || ferramenta.getSelectedIndex() == 2 || ferramenta.getSelectedIndex() == 7) {
                LayoutAreas.Area antes = areaSelecionada();
                if (antes != null) {
                    areas.marcar(chave(), new LayoutAreas.Area(antes.x, antes.y, w, h));
                }
            } else if (ferramenta.getSelectedIndex() == 3) {
                IconesTabuleiro.IconeTributo tributo = tributoSelecionado();
                IconesTabuleiro.IconeCidade cidade = cidadeSelecionada();
                if (tributo != null) {
                    tributo.posicionar(tributo.getX(), tributo.getY(), w);
                } else if (cidade != null) {
                    cidade.posicionar(cidade.getX(), cidade.getY(), w);
                }
            } else if ((ferramenta.getSelectedIndex() == 4
                    || ferramenta.getSelectedIndex() == 5) && raios != null) {
                raios.definir(chaveMarcador(), w);
            }
            alterado = true;
            tela.repaint();
        } catch (IllegalArgumentException erro) {
            mensagem.setText(erro.getMessage());
        }
    }

    /** Edicao da topologia disponivel na mesma janela, sem abrir um segundo editor. */
    private void editarConexao(boolean conectar) {
        if (mapa == null || ferramenta.getSelectedIndex() != 0 || idParada() < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione Tabuleiro > Paradas e escolha a primeira parada.");
            return;
        }
        List<Integer> destinos = new ArrayList<>();
        for (Integer id : mapa.getPontos().keySet()) {
            if (id != idParada()) {
                destinos.add(id);
            }
        }
        Integer destino = (Integer) JOptionPane.showInputDialog(this,
                "Selecione a segunda parada:",
                conectar ? "Conectar paradas" : "Desconectar paradas",
                JOptionPane.PLAIN_MESSAGE, null, destinos.toArray(),
                destinos.isEmpty() ? null : destinos.get(0));
        if (destino == null) {
            return;
        }
        boolean sucesso = conectar ? mapa.conectar(idParada(), destino)
                : mapa.desconectar(idParada(), destino);
        if (sucesso) {
            alterado = true;
            mensagem.setText("Conexao atualizada. Salve as edicoes para aplicar.");
            tela.repaint();
        } else {
            mensagem.setText("A conexao ja existe ou nao pode ser alterada.");
        }
    }

    private void associarRecursosKhan() {
        if (icones == null || recursosKhan == null || chave() == null
                || ferramenta.getSelectedIndex() != 6) {
            JOptionPane.showMessageDialog(this,
                    "Escolha Tabuleiro > Recursos controlados pelo Khan > destino.");
            return;
        }
        DestinoKhan destino = DestinoKhan.valueOf(chave().substring(5));
        if (!destino.ehProvincia()) {
            JOptionPane.showMessageDialog(this, "A area de melhorias nao repoem recursos do mapa.");
            return;
        }
        JPanel painel = new JPanel(new java.awt.GridLayout(0, 1, 3, 3));
        List<javax.swing.JCheckBox> seletores = new ArrayList<javax.swing.JCheckBox>();
        for (int i = 0; i < icones.getTributos().size(); i++) {
            IconesTabuleiro.IconeTributo icone = icones.getTributos().get(i);
            StringBuilder nome = new StringBuilder();
            for (br.herdeiros.model.Provincia provincia : icone.getProvincias()) {
                if (nome.length() > 0) {
                    nome.append(", ");
                }
                nome.append(provincia.name());
            }
            javax.swing.JCheckBox marcar = new javax.swing.JCheckBox(
                    "Icone " + i + " - " + icone.getTipo() + " - " + nome,
                    recursosKhan.consultar(destino).contains(i));
            seletores.add(marcar);
            painel.add(marcar);
        }
        javax.swing.JScrollPane rolagem = new javax.swing.JScrollPane(painel);
        rolagem.setPreferredSize(new Dimension(530, 340));
        int escolha = JOptionPane.showConfirmDialog(this, rolagem,
                "Recursos repostos por " + destino.getDescricao(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (escolha == JOptionPane.OK_OPTION) {
            java.util.Set<Integer> indices = new java.util.LinkedHashSet<Integer>();
            for (int i = 0; i < seletores.size(); i++) {
                if (seletores.get(i).isSelected()) {
                    indices.add(i);
                }
            }
            if (indices.size() != 3) {
                JOptionPane.showMessageDialog(this,
                        "Associe exatamente 3 icones de tributo a este destino do Khan.",
                        "Mapeamento do Khan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            recursosKhan.definir(destino, indices);
            alterado = true;
            mensagem.setText("Recursos associados a " + destino.getDescricao()
                    + ". Salve todas as edicoes.");
            tela.repaint();
        }
    }

    /** Configura individualmente quais paradas permitem coletar a ficha selecionada. */
    private void editarParadasTributo() {
        IconesTabuleiro.IconeTributo tributo = tributoSelecionado();
        if (tributo == null || mapa == null || ferramenta.getSelectedIndex() != 3) {
            JOptionPane.showMessageDialog(this,
                    "Selecione Tabuleiro > Icones de recursos / cidades > tributo.N.");
            return;
        }
        JPanel lista = new JPanel(new java.awt.GridLayout(0, 4, 5, 5));
        java.util.List<Integer> ids = new ArrayList<Integer>(mapa.getPontos().keySet());
        java.util.Collections.sort(ids);
        java.util.List<javax.swing.JCheckBox> caixas = new ArrayList<javax.swing.JCheckBox>();
        for (Integer id : ids) {
            javax.swing.JCheckBox caixa = new javax.swing.JCheckBox("Parada " + id,
                    tributo.getParadasPermitidas().contains(id));
            lista.add(caixa);
            caixas.add(caixa);
        }
        javax.swing.JScrollPane rolagem = new javax.swing.JScrollPane(lista);
        rolagem.setPreferredSize(new Dimension(620, 330));
        int resultado = JOptionPane.showConfirmDialog(this, rolagem,
                "Paradas permitidas para " + chave()
                + " (nenhuma marcada = acesso padrao da provincia)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) return;
        java.util.Set<Integer> selecionadas = new java.util.LinkedHashSet<Integer>();
        for (int i = 0; i < caixas.size(); i++) {
            if (caixas.get(i).isSelected()) selecionadas.add(ids.get(i));
        }
        tributo.definirParadasPermitidas(selecionadas);
        alterado = true;
        mensagem.setText("Paradas de coleta de " + chave() + " atualizadas. Salve as edicoes.");
    }

    private boolean salvar() {
        try {
            if (mapa != null) {
                ArquivoMapa.salvar(mapa, Paths.get("assets/mapa_paradas.csv"));
            }
            areas.salvar();
            marcadores.salvar();
            if (raios != null) {
                raios.salvar();
            }
            if (icones != null) {
                icones.salvar(Paths.get("assets/icones_tabuleiro.csv"));
            }
            if (recursosKhan != null) {
                recursosKhan.salvar();
            }
            alterado = false;
            mensagem.setText("CSV salvo. Reinicie a partida para recarregar as hitboxes.");
            return true;
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(), "Erro ao salvar", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private final class TelaEdicao extends JPanel {
        private static final long serialVersionUID = 1L;
        private int origemX;
        private int origemY;
        private int larguraImagem;
        private int alturaImagem;
        private double anteriorX;
        private double anteriorY;
        private boolean arrastando;

        TelaEdicao() {
            setBackground(new Color(38, 38, 42));
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    double[] ponto = coordenada(e);
                    if (ponto == null) {
                        return;
                    }
                    anteriorX = ponto[0];
                    anteriorY = ponto[1];
                    arrastando = true;
                    posicionar(ponto[0], ponto[1]);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    double[] ponto = coordenada(e);
                    if (!arrastando || ponto == null) {
                        return;
                    }
                    if (ferramenta.getSelectedIndex() <= 2 || ferramenta.getSelectedIndex() == 7) {
                        double w = Math.max(0.004, Math.abs(ponto[0] - anteriorX));
                        double h = Math.max(0.004, Math.abs(ponto[1] - anteriorY));
                        if (ferramenta.getSelectedIndex() == 0) {
                            // Para paradas, a medida corresponde ao semieixo da elipse.
                            if (w <= 0.35 && h <= 0.35) {
                                preenchendo = true;
                                largura.setValue(w * 100);
                                altura.setValue(h * 100);
                                preenchendo = false;
                                dimensoesAlteradas();
                            }
                        } else {
                            // Para cartas, a medida corresponde a meia largura/altura.
                            if (w * 2 <= 0.35 && h * 2 <= 0.35) {
                                preenchendo = true;
                                largura.setValue(w * 200);
                                altura.setValue(h * 200);
                                preenchendo = false;
                                dimensoesAlteradas();
                            }
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    arrastando = false;
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private double[] coordenada(MouseEvent e) {
            if (imagem == null || larguraImagem == 0 || alturaImagem == 0) {
                return null;
            }
            double x = (e.getX() - origemX) / (double) larguraImagem;
            double y = (e.getY() - origemY) / (double) alturaImagem;
            if (x < 0 || x > 1 || y < 0 || y > 1) {
                return null;
            }
            return new double[] {x, y};
        }

        private void posicionar(double x, double y) {
            if (chave() == null) {
                return;
            }
            if (ferramenta.getSelectedIndex() == 0 && mapa != null) {
                mapa.mover(idParada(), x, y);
            } else if (ferramenta.getSelectedIndex() == 1 || ferramenta.getSelectedIndex() == 2 || ferramenta.getSelectedIndex() == 7) {
                areas.marcar(chave(), new LayoutAreas.Area(x, y,
                        ((Number) largura.getValue()).doubleValue() / 100,
                        ((Number) altura.getValue()).doubleValue() / 100));
                marcadores.marcar(chave(), x, y);
            } else if (ferramenta.getSelectedIndex() == 3) {
                IconesTabuleiro.IconeTributo tributo = tributoSelecionado();
                IconesTabuleiro.IconeCidade cidade = cidadeSelecionada();
                double raio = ((Number) largura.getValue()).doubleValue() / 100;
                if (tributo != null) {
                    tributo.posicionar(x, y, raio);
                } else if (cidade != null) {
                    cidade.posicionar(x, y, raio);
                }
            } else if (ferramenta.getSelectedIndex() == 4
                    || ferramenta.getSelectedIndex() == 5) {
                marcadores.marcar(chaveMarcador(), x, y);
            }
            alterado = true;
            mensagem.setText("Alterado " + chave() + ". Clique em Salvar todas as edicoes.");
            repaint();
        }

        private LayoutAreas.Area areaPadraoBonus(String id) {
            if ("karakorum.especial".equals(id)) return new LayoutAreas.Area(0.495, 0.38, 0.09, 0.09);
            if (mapa == null || !id.startsWith("bonus.")) return null;
            try {
                MapaDados.Ponto ponto = mapa.getPonto(Integer.parseInt(id.substring(6)));
                if (ponto == null) return null;
                return new LayoutAreas.Area(ponto.getX(), Math.max(0.035, ponto.getY() - 0.05), 0.052, 0.052);
            } catch (NumberFormatException erro) { return null; }
        }

        private void desenharCirculo(Graphics2D g, String id, double x, double y, double raio) {
            int cx = origemX + (int) Math.round(x * larguraImagem);
            int cy = origemY + (int) Math.round(y * alturaImagem);
            int rx = (int) Math.round(raio * larguraImagem);
            int ry = (int) Math.round(raio * alturaImagem);
            boolean selecionado = chave() != null && chave().startsWith(id)
                    && (chave().length() == id.length() || chave().charAt(id.length()) == ' ');
            g.setColor(selecionado ? Color.YELLOW : new Color(0, 255, 200, 165));
            g.drawOval(cx - rx, cy - ry, rx * 2, ry * 2);
            if (selecionado) {
                g.drawString(id, cx, cy);
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (imagem == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double escala = Math.min(getWidth() / (double) imagem.getWidth(),
                    getHeight() / (double) imagem.getHeight());
            larguraImagem = (int) Math.round(imagem.getWidth() * escala);
            alturaImagem = (int) Math.round(imagem.getHeight() * escala);
            origemX = (getWidth() - larguraImagem) / 2;
            origemY = (getHeight() - alturaImagem) / 2;
            g.drawImage(imagem, origemX, origemY, larguraImagem, alturaImagem, null);
            g.setStroke(new BasicStroke(2));
            if (ferramenta.getSelectedIndex() == 0 && mapa != null) {
                for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
                    int cx = origemX + (int) (ponto.getX() * larguraImagem);
                    int cy = origemY + (int) (ponto.getY() * alturaImagem);
                    int rx = (int) (ponto.getRaio() * Math.min(larguraImagem, alturaImagem));
                    int ry = (int) (ponto.getRaioVertical() * Math.min(larguraImagem, alturaImagem));
                    g.setColor(ponto.getId() == idParada() ? Color.YELLOW : new Color(0, 255, 200, 165));
                    g.drawOval(cx - rx, cy - ry, rx * 2, ry * 2);
                    g.drawString(String.valueOf(ponto.getId()), cx, cy);
                }
            } else if (ferramenta.getSelectedIndex() == 1 || ferramenta.getSelectedIndex() == 2 || ferramenta.getSelectedIndex() == 7) {
                List<String> chaves = new ArrayList<>();
                for (int i = 0; i < elemento.getItemCount(); i++) {
                    chaves.add(elemento.getItemAt(i));
                }
                for (String id : chaves) {
                    LayoutAreas.Area area = areas.consultar(id);
                    if (area == null) {
                        LayoutMarcadores.Posicao centro = marcadores.consultar(id);
                        if (centro == null) {
                            area = ferramenta.getSelectedIndex() == 7 ? areaPadraoBonus(id) : null;
                            if (area == null) continue;
                        }
                        if (area == null) {
                        boolean tabuleiro = imagemSelecionada.getSelectedIndex() == 0;
                        area = new LayoutAreas.Area(centro.getX(), centro.getY(),
                                tabuleiro ? (ferramenta.getSelectedIndex() == 7 ? 0.052 : 76.0 / 900) : 218.0 / 1650,
                                tabuleiro ? 40.0 / 900 : 114.0 / 898);
                        }
                    }
                    int x = origemX + (int) ((area.x - area.largura / 2) * larguraImagem);
                    int y = origemY + (int) ((area.y - area.altura / 2) * alturaImagem);
                    int w = (int) (area.largura * larguraImagem);
                    int h = (int) (area.altura * alturaImagem);
                    g.setColor(id.equals(chave()) ? Color.YELLOW : new Color(0, 255, 200, 165));
                    g.drawRect(x, y, w, h);
                }
            } else if (ferramenta.getSelectedIndex() == 3 && icones != null) {
                for (int i = 0; i < icones.getTributos().size(); i++) {
                    IconesTabuleiro.IconeTributo tributo = icones.getTributos().get(i);
                    String id = "tributo." + i;
                    desenharCirculo(g, id, tributo.getX(), tributo.getY(), tributo.getRaio());
                }
                for (int i = 0; i < icones.getCidades().size(); i++) {
                    IconesTabuleiro.IconeCidade cidade = icones.getCidades().get(i);
                    desenharCirculo(g, "cidade." + i, cidade.getX(), cidade.getY(), cidade.getRaio());
                }
            } else if (ferramenta.getSelectedIndex() == 6 && icones != null
                    && chave() != null && recursosKhan != null) {
                DestinoKhan destino = DestinoKhan.valueOf(chave().substring(5));
                for (int i = 0; i < icones.getTributos().size(); i++) {
                    IconesTabuleiro.IconeTributo icone = icones.getTributos().get(i);
                    int cx = origemX + (int) Math.round(icone.getX() * larguraImagem);
                    int cy = origemY + (int) Math.round(icone.getY() * alturaImagem);
                    int raio = Math.max(5, (int) Math.round(icone.getRaio() * larguraImagem));
                    g.setColor(recursosKhan.consultar(destino).contains(i)
                            ? Color.YELLOW : new Color(0, 255, 200, 165));
                    g.drawOval(cx - raio, cy - raio, raio * 2, raio * 2);
                    g.drawString(String.valueOf(i), cx + raio, cy);
                }
            } else if (ferramenta.getSelectedIndex() == 4 || ferramenta.getSelectedIndex() == 5) {
                for (int i = 0; i < elemento.getItemCount(); i++) {
                    String id = elemento.getItemAt(i);
                    LayoutMarcadores.Posicao centro = marcadores.consultar(id);
                    if (centro == null && id.startsWith("khan.")) {
                        DestinoKhan destino = DestinoKhan.valueOf(id.substring(5));
                        centro = new LayoutMarcadores.Posicao(destino.getX(), destino.getY());
                    }
                    if (centro != null) {
                        double raio = raios.consultar(id, id.startsWith("khan.")
                                ? Math.sqrt(0.0030) : 16.0 / 900);
                        desenharCirculo(g, id, centro.getX(), centro.getY(), raio);
                    }
                }
            }
            g.dispose();
        }
    }
}
