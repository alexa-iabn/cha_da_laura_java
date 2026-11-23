package com.mycompany.cha_da_laura_java;

import com.mycompany.cha_da_laura_java.paginaEstoque;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.Locale;
import java.util.function.DoubleFunction;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


public class paginaDashboard extends JPanel {

    
    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color VERDE_COLUNA = new Color(140, 190, 145);
    private static final Color VERDE_BARRA = new Color(110, 165, 115);
    private static final Color VERDE_SUBIU = new Color(46, 125, 50);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color AZUL_LINHA = new Color(33, 118, 210);
    private static final Color VERMELHO = new Color(211, 47, 47);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color CINZA_TRILHO = new Color(240, 242, 240);
    private static final Color BRANCO = Color.WHITE;
    private static final String FONTE = "Segoe UI";
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    public paginaDashboard() {
        setLayout(new BorderLayout());
        setBackground(BRANCO);

        JPanel conteudo = new PainelLarguraJanela();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(BRANCO);
        conteudo.setBorder(new EmptyBorder(20, 30, 20, 30));

        JComponent[] partes = {
            criarCabecalho(),
            criarLinhaKpis(),
            criarLinhaGraficos(),
            criarLinhaEstoque(),
            criarUltimasMovimentacoes()
        };
        int[] espacos = {16, 16, 16, 16, 0};
        for (int i = 0; i < partes.length; i++) {
            partes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            conteudo.add(partes[i]);
            conteudo.add(Box.createVerticalStrut(espacos[i]));
        }

        JPanel envoltorio = new PainelLarguraJanela();
        envoltorio.setLayout(new BorderLayout());
        envoltorio.setBackground(BRANCO);
        envoltorio.add(conteudo, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel criarCabecalho() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(new Font(FONTE, Font.BOLD, 32));
        titulo.setForeground(AZUL_TITULO);
        JLabel subtitulo = new JLabel("Visão geral do seu negócio.");
        subtitulo.setFont(new Font(FONTE, Font.PLAIN, 15));
        subtitulo.setForeground(AZUL_TEXTO);
        p.add(titulo);
        p.add(subtitulo);
        return p;
    }

    private JPanel criarLinhaKpis() {
        // TODO: calcular com consultas no banco (ex.: SELECT COUNT(*) FROM produto ...)
        return linha(new int[]{1, 1, 1, 1},
            criarKpi("Produtos em estoque", "42", "8%", true),
            criarKpi("Insumos em estoque", "20", "12%", false),
            criarKpi("Produções realizadas", "18", "20%", true),
            criarKpi("Total de saídas", "56", "14%", true));
    }

    private JPanel criarLinhaGraficos() {
        // TODO: buscar no banco a produção por produto e as saídas por mês
        JPanel producao = criarCard("Produção por produto", null,
            new GraficoColunas(
                new String[]{"Blend\nCalme", "Blend\nFelicità", "Blend\nOrmoni", "Blend\nMaterniTea", "Blend\nAirmid", "Chai\nMasala"},
                new double[]{32, 25, 20, 15, 10, 8}));
        JPanel saidas = criarCard("Evolução das saídas", null,
            new GraficoLinha(
                new String[]{"Abr", "Mai", "Jun", "Jul", "Ago", "Set"},
                new double[]{12, 31, 40, 57, 48, 70}));
        return linha(new int[]{1, 1}, producao, saidas);
    }

    private JPanel criarLinhaEstoque() {
        // TODO: buscar no banco
        JPanel composicao = criarCard("Composição de estoque (insumos)", null,
            new GraficoRosca(
                new String[]{"Ervas", "Especiarias", "Frutas", "Flores", "Adoçantes", "Outros"},
                new double[]{35, 20, 15, 10, 8, 12},
                new Color[]{new Color(52, 105, 60), new Color(150, 215, 150), new Color(190, 215, 250),
                    new Color(235, 220, 185), new Color(250, 200, 210), new Color(215, 215, 215)},
                "20", "insumos"));

        JPanel consumidos = criarCard("Insumos mais consumidos", null,
            new GraficoBarrasH(
                new String[]{"Camomila", "Hortelã", "Erva-doce", "Hibisco", "Chá verde"},
                new double[]{12, 9, 7, 6, 5},
                v -> numero(v) + " kg"));

        JPanel estoqueBaixo = criarCard("Estoque baixo", criarLink("Ver tudo", () -> abrirPagina(new paginaEstoque())),
            criarTabela(
                new String[]{"Nome", "Tipo", "Atual", "Mínimo"},
                new Object[][]{
                    {"Camomila", "Insumo", 5, 10},
                    {"Hortelã", "Insumo", 7, 10},
                    {"Blend Ormoni (30g)", "Produto", 6, 10},
                    {"Melissa", "Insumo", 8, 10},
                    {"Chai Masala (80g)", "Produto", 5, 10}
                }, 26));

        return linha(new int[]{12, 10, 12}, composicao, consumidos, estoqueBaixo);
    }

    private JPanel criarUltimasMovimentacoes() {
        // TODO: SELECT ... FROM movimentacao ORDER BY data DESC LIMIT 5
        JPanel card = criarCard("Últimas movimentações",
            criarLink("Ver todas", () -> abrirPagina(new paginaEstoque())),
            criarTabela(
                new String[]{"Data", "Tipo", "Descrição", "Produto/Insumo", "Quantidade", "Responsável"},
                new Object[][]{
                    {"29/09/2026", "Saída", "Venda (cliente)", "Blend Calme", "5 un.", "Usuário"},
                    {"28/09/2026", "Produção", "Produção realizada", "Blend Felicità", "20 un.", "Usuário"},
                    {"27/09/2026", "Entrada", "Compra (fornecedor)", "Camomila", "2 kg", "Usuário"},
                    {"27/09/2026", "Saída", "E-commerce", "Blend Ormoni", "3 un.", "Usuário"},
                    {"26/09/2026", "Produção", "Produção realizada", "Chai Masala", "15 un.", "Usuário"}
                }, 28));
        card.setPreferredSize(null); // altura conforme a tabela
        return card;
    }

    private void abrirPagina(JPanel novaPagina) {
        Container pai = getParent();
        if (pai == null) {
            return;
        }
        pai.removeAll();
        pai.setLayout(new BorderLayout());
        pai.add(novaPagina, BorderLayout.CENTER);
        pai.revalidate();
        pai.repaint();
    }

    
    /** Coloca os cards lado a lado; "pesos" define a largura relativa de cada um. */
    private JPanel linha(int[] pesos, JComponent... cards) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        for (int i = 0; i < cards.length; i++) {
            g.gridx = i;
            g.weightx = pesos[i];
            g.insets = new Insets(0, 0, 0, i < cards.length - 1 ? 14 : 0);
            cards[i].setPreferredSize(new Dimension(10, cards[i].getPreferredSize().height));
            p.add(cards[i], g);
        }
        return p;
    }

    private JPanel criarKpi(String titulo, String valor, String variacao, boolean subiu) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true),
                BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 4, 0, 0, VERDE), new EmptyBorder(14, 18, 14, 18))));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font(FONTE, Font.PLAIN, 13));
        lblTitulo.setForeground(AZUL_TITULO);

        JPanel linhaValor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaValor.setOpaque(false);
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font(FONTE, Font.BOLD, 28));
        lblValor.setForeground(AZUL_TITULO);
        JLabel lblVariacao = new JLabel((subiu ? "↑ " : "↓ ") + variacao);
        lblVariacao.setFont(new Font(FONTE, Font.PLAIN, 14));
        lblVariacao.setForeground(subiu ? VERDE_SUBIU : VERMELHO);
        linhaValor.add(lblValor);
        linhaValor.add(Box.createHorizontalStrut(14));
        linhaValor.add(lblVariacao);

        JLabel lblDescricao = new JLabel("em relação ao período anterior");
        lblDescricao.setFont(new Font(FONTE, Font.PLAIN, 12));
        lblDescricao.setForeground(AZUL_TEXTO);

        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDescricao.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitulo);
        card.add(linhaValor);
        card.add(lblDescricao);
        return card;
    }

    /** Card com título (e um link opcional à direita) e um conteúdo embaixo. */
    private JPanel criarCard(String titulo, JComponent link, JComponent conteudo) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.BOLD, 16));
        t.setForeground(AZUL_TITULO);
        topo.add(t, BorderLayout.WEST);
        if (link != null) {
            topo.add(link, BorderLayout.EAST);
        }

        card.add(topo, BorderLayout.NORTH);
        card.add(conteudo, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(300, 230));
        return card;
    }

    private JLabel criarLink(String texto, Runnable aoClicar) {
        JLabel link = new JLabel(texto + "  →");
        link.setFont(new Font(FONTE, Font.PLAIN, 13));
        link.setForeground(VERDE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                aoClicar.run();
            }
        });
        return link;
    }

    
    private JComponent criarTabela(String[] colunas, Object[][] linhas, int alturaLinha) {
        DefaultTableModel modelo = new DefaultTableModel(linhas, colunas) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(alturaLinha);
        tabela.setGridColor(BORDA);
        tabela.setFocusable(false);
        tabela.setRowSelectionAllowed(false);
        tabela.setDefaultRenderer(Object.class, new Renderizador());
        if (colunas.length == 4) { // tabela "Estoque baixo": nome mais largo
            int[] larguras = {140, 65, 55, 70};
            for (int i = 0; i < larguras.length; i++) {
                tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
            }
        }

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 28));
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setFont(new Font(FONTE, Font.BOLD, 12));
                setBackground(new Color(244, 246, 244));
                setForeground(AZUL_TITULO);
                setHorizontalAlignment(ehNumero(String.valueOf(valor)) ? CENTER : LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 1, BORDA), new EmptyBorder(0, 8, 0, 8)));
                return this;
            }
        });

        JPanel caixa = new JPanel(new BorderLayout());
        caixa.setBorder(new LineBorder(BORDA));
        caixa.add(cabecalho, BorderLayout.NORTH);
        caixa.add(tabela, BorderLayout.CENTER);

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(caixa, BorderLayout.NORTH);
        return envoltorio;
    }

   
    private static boolean ehNumero(String coluna) {
        return coluna.equals("Atual") || coluna.equals("Mínimo");
    }

    private static String numero(double v) {
        return (v == Math.floor(v))
                ? String.format(BR, "%,.0f", v)
                : String.format(BR, "%,.1f", v);
    }

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    /** Calcula uma escala "redonda" para o eixo Y. Retorna {passo, máximo}. */
    private static double[] escala(double maior) {
        double bruto = Math.max(maior, 1) / 4;
        double magnitude = Math.pow(10, Math.floor(Math.log10(bruto)));
        double normal = bruto / magnitude;
        double passo = (normal <= 1 ? 1 : normal <= 2 ? 2 : normal <= 2.5 ? 2.5 : normal <= 5 ? 5 : 10) * magnitude;
        double maximo = passo * 4;
        while (maximo < maior) {
            maximo += passo;
        }
        return new double[]{passo, maximo};
    }

    
    private static class Selo extends JLabel {
        private Color corSelo = Color.LIGHT_GRAY;

        Selo() {
            setHorizontalAlignment(CENTER);
            setFont(new Font(FONTE, Font.PLAIN, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(BRANCO);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 22;
            int h = 19;
            g2.setColor(corSelo);
            g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Renderizador extends DefaultTableCellRenderer {
        private final Selo selo = new Selo();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
            String nomeColuna = t.getColumnName(coluna);
            String texto = (valor == null) ? "" : valor.toString();

            if (nomeColuna.equals("Tipo") && t.getModel().getColumnCount() > 4) { // tabela de movimentações
                selo.setText(texto);
                switch (texto) {
                    case "Saída":
                        selo.corSelo = new Color(253, 224, 226);
                        selo.setForeground(new Color(198, 40, 40));
                        break;
                    case "Entrada":
                        selo.corSelo = new Color(220, 234, 252);
                        selo.setForeground(new Color(25, 90, 180));
                        break;
                    default: // Produção
                        selo.corSelo = new Color(220, 240, 220);
                        selo.setForeground(new Color(30, 110, 50));
                }
                return selo;
            }

            setBackground(BRANCO);
            setFont(new Font(FONTE, Font.PLAIN, 12));
            setForeground(AZUL_TITULO);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(ehNumero(nomeColuna) ? CENTER : LEFT);
            if (nomeColuna.equals("Atual")) {
                setForeground(VERMELHO); // nesta tabela todos estão abaixo do mínimo
            }
            return this;
        }
    }

    

    
    private static class GraficoColunas extends JComponent {
        private final String[] rotulos;
        private final double[] valores;

        GraficoColunas(String[] rotulos, double[] valores) {
            this.rotulos = rotulos;
            this.valores = valores;
            setPreferredSize(new Dimension(300, 170));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setFont(new Font(FONTE, Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            double maior = 0;
            for (double v : valores) {
                maior = Math.max(maior, v);
            }
            double[] esc = escala(maior);
            double passo = esc[0];
            double maximo = esc[1];

            int esquerda = fm.stringWidth(numero(maximo)) + 10;
            int topo = 6;
            int base = getHeight() - 32; // espaço para rótulos de 2 linhas
            int altura = base - topo;
            int direita = getWidth() - 4;

            for (double v = 0; v <= maximo + 0.001; v += passo) {
                int y = base - (int) (altura * v / maximo);
                g2.setColor(new Color(235, 238, 235));
                g2.drawLine(esquerda, y, direita, y);
                g2.setColor(AZUL_TEXTO);
                String r = numero(v);
                g2.drawString(r, esquerda - 6 - fm.stringWidth(r), y + 4);
            }

            double grupo = (direita - esquerda) / (double) valores.length;
            int larguraBarra = (int) (grupo * 0.55);
            for (int i = 0; i < valores.length; i++) {
                int h = (int) (altura * valores[i] / maximo);
                int x = (int) (esquerda + i * grupo + (grupo - larguraBarra) / 2);
                g2.setColor(VERDE_COLUNA);
                g2.fillRect(x, base - h, larguraBarra, h);

                g2.setColor(AZUL_TITULO);
                String[] partes = rotulos[i].split("\n");
                for (int k = 0; k < partes.length; k++) {
                    int xt = (int) (esquerda + i * grupo + (grupo - fm.stringWidth(partes[k])) / 2);
                    g2.drawString(partes[k], xt, base + 14 + k * 13);
                }
            }
            g2.dispose();
        }
    }

    private static class GraficoLinha extends JComponent {
        private final String[] rotulos;
        private final double[] valores;

        GraficoLinha(String[] rotulos, double[] valores) {
            this.rotulos = rotulos;
            this.valores = valores;
            setPreferredSize(new Dimension(300, 170));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setFont(new Font(FONTE, Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            double maior = 0;
            for (double v : valores) {
                maior = Math.max(maior, v);
            }
            double[] esc = escala(maior);
            double passo = esc[0];
            double maximo = esc[1];

            int esquerda = fm.stringWidth(numero(maximo)) + 14;
            int topo = 6;
            int base = getHeight() - 20;
            int altura = base - topo;
            int direita = getWidth() - 10;

            for (double v = 0; v <= maximo + 0.001; v += passo) {
                int y = base - (int) (altura * v / maximo);
                g2.setColor(new Color(235, 238, 235));
                g2.drawLine(esquerda, y, direita, y);
                g2.setColor(AZUL_TEXTO);
                String r = numero(v);
                g2.drawString(r, esquerda - 8 - fm.stringWidth(r), y + 4);
            }

            int n = valores.length;
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = esquerda + (int) ((direita - esquerda) * (n == 1 ? 0.5 : i / (double) (n - 1)));
                ys[i] = base - (int) (altura * valores[i] / maximo);
            }

            // área azul clara embaixo da linha
            Path2D area = new Path2D.Double();
            area.moveTo(xs[0], base);
            for (int i = 0; i < n; i++) {
                area.lineTo(xs[i], ys[i]);
            }
            area.lineTo(xs[n - 1], base);
            area.closePath();
            g2.setColor(new Color(33, 118, 210, 40));
            g2.fill(area);

            // linha
            g2.setColor(AZUL_LINHA);
            g2.setStroke(new BasicStroke(2f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
            }
            // pontos e rótulos dos meses
            for (int i = 0; i < n; i++) {
                g2.setColor(AZUL_LINHA);
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2.setColor(AZUL_TITULO);
                g2.drawString(rotulos[i], xs[i] - fm.stringWidth(rotulos[i]) / 2, base + 15);
            }
            g2.dispose();
        }
    }

    private static class GraficoRosca extends JComponent {
        private final String[] nomes;
        private final double[] valores;
        private final Color[] cores;
        private final String textoCentro;
        private final String subtextoCentro;

        GraficoRosca(String[] nomes, double[] valores, Color[] cores, String textoCentro, String subtextoCentro) {
            this.nomes = nomes;
            this.valores = valores;
            this.cores = cores;
            this.textoCentro = textoCentro;
            this.subtextoCentro = subtextoCentro;
            setPreferredSize(new Dimension(300, 170));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            double total = 0;
            for (double v : valores) {
                total += v;
            }
            int d = Math.min(getHeight() - 6, (int) (getWidth() * 0.44));
            int x = 2;
            int y = (getHeight() - d) / 2;

            double inicio = 90;
            for (int i = 0; i < valores.length; i++) {
                double angulo = 360 * valores[i] / total;
                g2.setColor(cores[i]);
                g2.fill(new Arc2D.Double(x, y, d, d, inicio, -angulo, Arc2D.PIE));
                inicio -= angulo;
            }
            int dFuro = (int) (d * 0.62);
            g2.setColor(BRANCO);
            g2.fillOval(x + (d - dFuro) / 2, y + (d - dFuro) / 2, dFuro, dFuro);

            g2.setFont(new Font(FONTE, Font.BOLD, 22));
            FontMetrics fmGrande = g2.getFontMetrics();
            g2.setColor(AZUL_TITULO);
            g2.drawString(textoCentro, x + (d - fmGrande.stringWidth(textoCentro)) / 2, y + d / 2 + 4);
            g2.setFont(new Font(FONTE, Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(subtextoCentro, x + (d - fm.stringWidth(subtextoCentro)) / 2, y + d / 2 + 20);

            // legenda
            int lx = x + d + 18;
            int ly = (getHeight() - nomes.length * 24) / 2 + 15;
            for (int i = 0; i < nomes.length; i++) {
                g2.setColor(cores[i]);
                g2.fillOval(lx, ly - 10, 11, 11);
                g2.setColor(AZUL_TITULO);
                g2.drawString(nomes[i], lx + 18, ly);
                String p = String.format(BR, "%.1f%%", 100 * valores[i] / total);
                g2.drawString(p, getWidth() - 4 - fm.stringWidth(p), ly);
                ly += 24;
            }
            g2.dispose();
        }
    }

    private static class GraficoBarrasH extends JComponent {
        private final String[] nomes;
        private final double[] valores;
        private final DoubleFunction<String> formato;

        GraficoBarrasH(String[] nomes, double[] valores, DoubleFunction<String> formato) {
            this.nomes = nomes;
            this.valores = valores;
            this.formato = formato;
            setPreferredSize(new Dimension(300, 170));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setFont(new Font(FONTE, Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();

            double maior = 0;
            int larguraNome = 0;
            int larguraValor = 0;
            for (int i = 0; i < nomes.length; i++) {
                maior = Math.max(maior, valores[i]);
                larguraNome = Math.max(larguraNome, fm.stringWidth(nomes[i]));
                larguraValor = Math.max(larguraValor, fm.stringWidth(formato.apply(valores[i])));
            }
            int alturaLinha = Math.min(30, getHeight() / nomes.length);
            int alturaBarra = 16;
            int y0 = (getHeight() - nomes.length * alturaLinha) / 2;
            int xBarra = larguraNome + 14;
            int larguraTrilho = Math.max(10, getWidth() - xBarra - larguraValor - 16);

            for (int i = 0; i < nomes.length; i++) {
                int y = y0 + i * alturaLinha + (alturaLinha - alturaBarra) / 2;
                int yTexto = y + (alturaBarra + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(CINZA_TRILHO);
                g2.fillRoundRect(xBarra, y, larguraTrilho, alturaBarra, 4, 4);
                g2.setColor(VERDE_BARRA);
                g2.fillRoundRect(xBarra, y, (int) (larguraTrilho * valores[i] / maior), alturaBarra, 4, 4);
                g2.setColor(AZUL_TITULO);
                g2.drawString(nomes[i], 0, yTexto);
                String v = formato.apply(valores[i]);
                g2.drawString(v, getWidth() - fm.stringWidth(v), yTexto);
            }
            g2.dispose();
        }
    }

    private static class PainelLarguraJanela extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientacao, int direcao) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientacao, int direcao) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}