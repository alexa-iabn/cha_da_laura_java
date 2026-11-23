package com.mycompany.cha_da_laura_java;

import java.awt.*;
import com.mycompany.cha_da_laura_java.dao.ProducaoDAO;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.MaskFormatter;


public class paginaProducao extends JPanel {

    // ===== Cores e fonte do layout =====
    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color VERMELHO = new Color(211, 47, 47);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color FUNDO_SECAO = new Color(241, 244, 239);
    private static final Color BRANCO = Color.WHITE;
    private static final String FONTE = "Segoe UI";
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String CONCLUIDA = "Concluída";
    private static final String EM_PRODUCAO = "Em produção";

    // TODO: trocar pelo nome do usuário que fez login
    private static final String USUARIO_LOGADO = "Maria";

    // ===== Dados (em memória por enquanto) =====
    private final Map<String, Double> estoqueInsumos = new LinkedHashMap<>(); // nome -> quantidade
    private final Map<String, String> unidadeInsumos = new LinkedHashMap<>(); // nome -> g / ml
    private final List<Receita> receitas = new ArrayList<>();
    private final List<Producao> producoes = new ArrayList<>();
    private final ProducaoDAO dao = new ProducaoDAO();
    private boolean carregamentoEmAndamento = false;
    private String telaAtual = "lista";

    // ===== Troca entre "lista" e "formulário" =====
    private final CardLayout telas = new CardLayout();
    private final JPanel painelTelas = new JPanel(telas);
    private final TelaLista telaLista;
    private final TelaFormulario telaFormulario;

    public paginaProducao() {
        setLayout(new BorderLayout());
        setBackground(BRANCO);

        telaLista = new TelaLista();
        telaFormulario = new TelaFormulario();
        painelTelas.setBackground(BRANCO);
        painelTelas.add(rolavel(telaLista), "lista");
        JPanel formularioComRodape = new JPanel(new BorderLayout());
        formularioComRodape.setBackground(BRANCO);
        JScrollPane scrollFormulario = new JScrollPane(telaFormulario);
        scrollFormulario.setBorder(null);
        scrollFormulario.getViewport().setBackground(BRANCO);
        scrollFormulario.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFormulario.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollFormulario.getVerticalScrollBar().setUnitIncrement(20);
        formularioComRodape.add(scrollFormulario, BorderLayout.CENTER);
        formularioComRodape.add(telaFormulario.botoes, BorderLayout.SOUTH);
        painelTelas.add(formularioComRodape, "formulario");
        add(painelTelas, BorderLayout.CENTER);

        telas.show(painelTelas, "lista");
        carregarBanco();
    }

    private void mostrarLista() {
        telaAtual = "lista";
        telas.show(painelTelas, "lista");
        carregarBanco();
    }

    private void mostrarFormulario() {
        telaAtual = "formulario";
        telas.show(painelTelas, "formulario");
        telaFormulario.limpar();
        carregarBanco();
    }

    // =====================================================================
    // MODELOS (no projeto real, use as classes do pacote "model")
    // =====================================================================
    private static class Receita {
        final String nome;
        final String produto;
        final int validadeDias;
        final int idReceita;
        final int rendimentoUnidades;
        final Map<String, Double> insumosPorUnidade = new LinkedHashMap<>(); // insumo -> quantidade por 1 un

        Receita(String nome, String produto, int validadeDias) {
            this(0, nome, produto, validadeDias, 1);
        }

        Receita(int idReceita, String nome, String produto, int validadeDias, int rendimentoUnidades) {
            this.idReceita = idReceita;
            this.rendimentoUnidades = rendimentoUnidades;
            this.nome = nome;
            this.produto = produto;
            this.validadeDias = validadeDias;
        }

        Receita insumo(String nome, double porUnidade) {
            insumosPorUnidade.put(nome, porUnidade);
            return this;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    private static class Producao {
        int id;
        LocalDate data;
        String produto;
        int quantidade;
        String receita;
        String status;
        String usuario;
        String lote;
        LocalDate validade;
        String observacoes;

        Producao(int id, LocalDate data, String produto, int quantidade, String receita,
                String status, String usuario, String lote, LocalDate validade, String observacoes) {
            this.id = id;
            this.data = data;
            this.produto = produto;
            this.quantidade = quantidade;
            this.receita = receita;
            this.status = status;
            this.usuario = usuario;
            this.lote = lote;
            this.validade = validade;
            this.observacoes = observacoes;
        }
    }

    // TELA 1: LISTA DE PRODUÇÕES
    private class TelaLista extends JPanel {
        private final JComboBox<String> cbPeriodo = new JComboBox<>(new String[]{
            "Todo o período", "Hoje", "Últimos 7 dias", "Últimos 30 dias", "Este mês"});
        private final JComboBox<String> cbProduto = new JComboBox<>();
        private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Todos os status", CONCLUIDA, EM_PRODUCAO});
        private final JComboBox<String> cbUsuario = new JComboBox<>();
        private final TabelaPaginada tabela;
        private boolean carregandoCombos = false;

        TelaLista() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BRANCO);
            setBorder(new EmptyBorder(20, 30, 20, 30));

            JComponent cabecalho = cabecalho("Produção", "Registre e acompanhe as produções realizadas.");
            cabecalho.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDA), new EmptyBorder(0, 0, 14, 0)));
            cabecalho.setMaximumSize(new Dimension(Integer.MAX_VALUE, cabecalho.getPreferredSize().height));

            JComboBox<?>[] combos = {cbPeriodo, cbProduto, cbStatus, cbUsuario};
            for (JComboBox<?> c : combos) {
                estilizarCombo(c, 160);
                c.addActionListener(e -> filtrosMudaram());
            }
            JButton btNova = botaoVerde("+  Nova produção");
            btNova.addActionListener(e -> mostrarFormulario());

            JPanel filtros = new JPanel(new GridBagLayout());
            filtros.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = 0;
            g.anchor = GridBagConstraints.SOUTH;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 0, 14);
            String[] rotulos = {"Período", "Produto", "Status", "Usuário"};
            for (int i = 0; i < combos.length; i++) {
                g.gridx = i;
                filtros.add(campo(rotulos[i], combos[i], false), g);
            }
            g.gridx = combos.length;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            g.insets = new Insets(0, 0, 0, 0);
            filtros.add(btNova, g);

            tabela = new TabelaPaginada(
                new String[]{"ID", "Data", "Produto", "Quantidade produzida", "Receita utilizada", "Status", "Usuário", "Ações"},
                "produções",
                objeto -> mostrarDetalhes((Producao) objeto));
            tabela.larguras(40, 100, 180, 150, 170, 120, 100, 70);

            JComponent[] partes = {cabecalho, filtros, tabela};
            int[] espacos = {16, 20, 0};
            for (int i = 0; i < partes.length; i++) {
                partes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                add(partes[i]);
                add(Box.createVerticalStrut(espacos[i]));
            }
        }

        private void filtrosMudaram() {
            if (!carregandoCombos) {
                tabela.voltarParaPrimeiraPagina();
                atualizar();
            }
        }

        void atualizar() {
            carregandoCombos = true;
            TreeSet<String> produtos = new TreeSet<>();
            TreeSet<String> usuarios = new TreeSet<>();
            for (Producao p : producoes) {
                produtos.add(p.produto);
                usuarios.add(p.usuario);
            }
            recarregarCombo(cbProduto, "Todos os produtos", produtos);
            recarregarCombo(cbUsuario, "Todos os usuários", usuarios);
            carregandoCombos = false;

            String periodo = (String) cbPeriodo.getSelectedItem();
            String produto = (String) cbProduto.getSelectedItem();
            String status = (String) cbStatus.getSelectedItem();
            String usuario = (String) cbUsuario.getSelectedItem();
            LocalDate hoje = LocalDate.now();

            List<Object[]> linhas = new ArrayList<>();
            List<Producao> objetos = new ArrayList<>();
            for (Producao p : producoes) {
                boolean noPeriodo;
                switch (periodo) {
                    case "Hoje": noPeriodo = p.data.equals(hoje); break;
                    case "Últimos 7 dias": noPeriodo = !p.data.isBefore(hoje.minusDays(6)); break;
                    case "Últimos 30 dias": noPeriodo = !p.data.isBefore(hoje.minusDays(29)); break;
                    case "Este mês": noPeriodo = p.data.getYear() == hoje.getYear() && p.data.getMonth() == hoje.getMonth(); break;
                    default: noPeriodo = true;
                }
                if (!noPeriodo
                        || (!"Todos os produtos".equals(produto) && !p.produto.equals(produto))
                        || (!"Todos os status".equals(status) && !p.status.equals(status))
                        || (!"Todos os usuários".equals(usuario) && !p.usuario.equals(usuario))) {
                    continue;
                }
                linhas.add(new Object[]{p.id, p.data.format(FORMATO_DATA), p.produto,
                    p.quantidade + " un", p.receita, p.status, p.usuario, "Ver"});
                objetos.add(p);
            }
            tabela.setLinhas(linhas, objetos);
        }

        private void mostrarDetalhes(Producao p) {
            StringBuilder texto = new StringBuilder();
            texto.append("Produção nº ").append(p.id)
                 .append("\nData: ").append(p.data.format(FORMATO_DATA))
                 .append("\nProduto: ").append(p.produto)
                 .append("\nQuantidade: ").append(p.quantidade).append(" un")
                 .append("\nReceita: ").append(p.receita)
                 .append("\nLote: ").append(p.lote)
                 .append("\nValidade: ").append(p.validade == null ? "-" : p.validade.format(FORMATO_DATA))
                 .append("\nStatus: ").append(p.status)
                 .append("\nUsuário: ").append(p.usuario);
            if (p.observacoes != null && !p.observacoes.isEmpty()) {
                texto.append("\nObservações: ").append(p.observacoes);
            }

            if (EM_PRODUCAO.equals(p.status)) {
                Object[] opcoes = {"Marcar como concluída", "Fechar"};
                int escolha = JOptionPane.showOptionDialog(this, texto.toString(), "Detalhes da produção",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[1]);
                if (escolha == 0) {
                    try {
                        dao.concluirRegistroAntigo(p.id);
                        mostrarLista();
                    } catch (SQLException ex) {
                        aviso("Erro ao concluir produção: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, texto.toString(), "Detalhes da produção",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // TELA 2: FORMULÁRIO "NOVA PRODUÇÃO"
    private class TelaFormulario extends JPanel implements Scrollable {
        private final JComboBox<String> cbProduto = new JComboBox<>();
        private final JComboBox<Receita> cbReceita = new JComboBox<>();
        private final CampoTexto txtQuantidade = new CampoTexto("Ex.: 10");
        private final CampoTexto txtLote = new CampoTexto("Ex.: L001");
        private final JFormattedTextField txtValidade = criarCampoData();
        private final JTextArea txtObservacoes = new JTextArea(3, 20);
        private final DefaultTableModel modeloInsumos = new DefaultTableModel(
                new String[]{"Insumo", "Quantidade necessária", "Unidade de medida", "Estoque atual", "Estoque após produção"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        private final JLabel lblAvisoInsumos = new JLabel(" ");
        private final JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        private boolean carregando = false;

        TelaFormulario() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BRANCO);
            setBorder(new EmptyBorder(16, 30, 20, 30));

            // --- Botão voltar + título ---
            JButton btVoltar = new JButton("←  Voltar");
            btVoltar.setFont(new Font(FONTE, Font.PLAIN, 14));
            btVoltar.setFocusPainted(false);
            btVoltar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(btVoltar, FUNDO_SECAO, AZUL_TITULO, FUNDO_SECAO);
            btVoltar.setBorder(new EmptyBorder(8, 16, 8, 16));
            btVoltar.addActionListener(e -> mostrarLista());

            JPanel linhaVoltar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            linhaVoltar.setOpaque(false);
            linhaVoltar.add(btVoltar);

            JComponent cabecalho = cabecalho("Nova produção", "Registre uma nova produção e atualize o estoque automaticamente.");

            // --- Campos ---
            estilizarCombo(cbProduto, 200);
            estilizarCombo(cbReceita, 200);
            txtObservacoes.setFont(new Font(FONTE, Font.PLAIN, 14));
            txtObservacoes.setLineWrap(true);
            txtObservacoes.setWrapStyleWord(true);

            cbProduto.addActionListener(e -> produtoMudou());
            cbReceita.addActionListener(e -> receitaMudou());
            txtQuantidade.getDocument().addDocumentListener(new AoDigitar(this::recalcularInsumos));

            // quantidade com "un" do lado
            JLabel lblUn = new JLabel("un", SwingConstants.CENTER);
            lblUn.setOpaque(true);
            lblUn.setBackground(FUNDO_SECAO);
            lblUn.setForeground(AZUL_TITULO);
            lblUn.setPreferredSize(new Dimension(44, 38));
            lblUn.setBorder(new MatteBorder(1, 0, 1, 1, BORDA));
            JPanel quantidadeComUnidade = new JPanel(new BorderLayout());
            quantidadeComUnidade.add(txtQuantidade, BorderLayout.CENTER);
            quantidadeComUnidade.add(lblUn, BorderLayout.EAST);

            JPanel infos = gradeCampos(new double[]{1, 1, 0.8},
                    campo("Produto", cbProduto, true),
                    campo("Receita", cbReceita, true),
                    campo("Quantidade produzida", quantidadeComUnidade, true));

            JPanel lotes = gradeCampos(new double[]{1, 1, 0.8},
                    campo("Número do lote", txtLote, true),
                    campo("Data de validade", txtValidade, true),
                    new JLabel());

            // --- Tabela de insumos (calculada) ---
            JTable tabelaInsumos = new JTable(modeloInsumos);
            tabelaInsumos.setRowHeight(28);
            tabelaInsumos.setGridColor(BORDA);
            tabelaInsumos.setFocusable(false);
            tabelaInsumos.setRowSelectionAllowed(false);
            tabelaInsumos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object valor,
                        boolean selecionado, boolean foco, int linha, int coluna) {
                    super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                    setBorder(new EmptyBorder(0, 12, 0, 8));
                    setFont(new Font(FONTE, Font.PLAIN, 13));
                    setBackground(BRANCO);
                    boolean faltando = coluna == 4 && String.valueOf(valor).startsWith("-");
                    setForeground(faltando ? VERMELHO : AZUL_TITULO);
                    return this;
                }
            });
            estilizarCabecalho(tabelaInsumos);
            JPanel caixaInsumos = new JPanel(new BorderLayout());
            caixaInsumos.setBorder(new LineBorder(BORDA, 1, true));
            caixaInsumos.add(tabelaInsumos.getTableHeader(), BorderLayout.NORTH);
            caixaInsumos.add(tabelaInsumos, BorderLayout.CENTER);
            lblAvisoInsumos.setFont(new Font(FONTE, Font.PLAIN, 13));
            lblAvisoInsumos.setForeground(AZUL_TEXTO);

            JPanel blocoInsumos = new JPanel(new BorderLayout(0, 6));
            blocoInsumos.setOpaque(false);
            blocoInsumos.add(caixaInsumos, BorderLayout.CENTER);
            blocoInsumos.add(lblAvisoInsumos, BorderLayout.SOUTH);

            JScrollPane rolagemObs = new JScrollPane(txtObservacoes);
            rolagemObs.setBorder(new LineBorder(BORDA, 1, true));
            rolagemObs.setPreferredSize(new Dimension(200, 64));

            // --- Botões ---
            JButton btCancelar = new JButton("Cancelar");
            btCancelar.setFont(new Font(FONTE, Font.PLAIN, 15));
            btCancelar.setFocusPainted(false);
            btCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(btCancelar, FUNDO_SECAO, AZUL_TITULO, BORDA);
            btCancelar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(11, 36, 11, 36)));
            btCancelar.addActionListener(e -> mostrarLista());

            JButton btSalvar = botaoVerde("Salvar produção");
            btSalvar.addActionListener(e -> salvar());

            botoes.setBackground(BRANCO);
            botoes.setBorder(new MatteBorder(1, 0, 0, 0, BORDA));
            botoes.add(btCancelar);
            botoes.add(btSalvar);

            // --- Card com as seções ---
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(BRANCO);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(12, 12, 14, 12)));
            JComponent[] partesCard = {
                tituloSecao("Informações da produção"), infos,
                tituloSecao("Lotes e validade"), lotes,
                tituloSecao("Insumos da receita"), blocoInsumos,
                tituloSecao("Observações (opcional)"), rolagemObs
            };
            int[] espacos = {10, 18, 10, 18, 10, 18, 10, 0};
            for (int i = 0; i < partesCard.length; i++) {
                partesCard[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(partesCard[i]);
                card.add(Box.createVerticalStrut(espacos[i]));
            }

            JComponent[] partes = {linhaVoltar, cabecalho, card};
            int[] espacosTela = {10, 14, 0};
            for (int i = 0; i < partes.length; i++) {
                partes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                add(partes[i]);
                add(Box.createVerticalStrut(espacosTela[i]));
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(100, visibleRect.height - 60);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        /** Deixa o formulário em branco toda vez que é aberto. */
        void limpar() {
            carregando = true;
            cbProduto.removeAllItems();
            cbProduto.addItem("Selecione o produto");
            TreeSet<String> produtos = new TreeSet<>();
            for (Receita r : receitas) {
                produtos.add(r.produto);
            }
            for (String p : produtos) {
                cbProduto.addItem(p);
            }
            cbProduto.setSelectedIndex(0);
            cbReceita.removeAllItems();
            carregando = false;

            txtQuantidade.setText("");
            txtLote.setText(sugerirLote());
            txtValidade.setValue(null);
            txtObservacoes.setText("");
            recalcularInsumos();
        }

        private void produtoMudou() {
            if (carregando) {
                return;
            }
            carregando = true;
            cbReceita.removeAllItems();
            String produto = (String) cbProduto.getSelectedItem();
            for (Receita r : receitas) {
                if (r.produto.equals(produto)) {
                    cbReceita.addItem(r);
                }
            }
            carregando = false;
            receitaMudou();
        }

        private void receitaMudou() {
            if (carregando) {
                return;
            }
            Receita r = (Receita) cbReceita.getSelectedItem();
            if (r != null) {
                txtValidade.setValue(LocalDate.now().plusDays(r.validadeDias).format(FORMATO_DATA));
            }
            recalcularInsumos();
        }

        private int lerQuantidade() {
            try {
                return Integer.parseInt(txtQuantidade.getText().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        /** Monta a tabela "Insumos da receita" multiplicando pela quantidade. */
        private boolean recalcularInsumos() {
            modeloInsumos.setRowCount(0);
            Receita r = (Receita) cbReceita.getSelectedItem();
            int quantidade = lerQuantidade();
            if (r == null) {
                lblAvisoInsumos.setText("Selecione o produto e a receita para ver os insumos.");
                lblAvisoInsumos.setForeground(AZUL_TEXTO);
                return false;
            }
            boolean falta = false;
            for (Map.Entry<String, Double> item : r.insumosPorUnidade.entrySet()) {
                String insumo = item.getKey();
                String unidade = unidadeInsumos.getOrDefault(insumo, "g");
                double necessario = BigDecimal.valueOf(item.getValue())
                        .multiply(BigDecimal.valueOf(quantidade))
                        .divide(BigDecimal.valueOf(Math.max(1, r.rendimentoUnidades)), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                double atual = estoqueInsumos.getOrDefault(insumo, 0.0);
                double depois = atual - necessario;
                if (depois < 0) {
                    falta = true;
                }
                modeloInsumos.addRow(new Object[]{insumo, numero(necessario), unidade,
                    numero(atual) + " " + unidade, numero(depois) + " " + unidade});
            }
            if (r.insumosPorUnidade.isEmpty()) {
                lblAvisoInsumos.setText("Esta receita não possui insumos cadastrados.");
                lblAvisoInsumos.setForeground(VERMELHO);
                return false;
            }
            if (quantidade <= 0) {
                lblAvisoInsumos.setText("Digite a quantidade produzida para calcular os insumos.");
                lblAvisoInsumos.setForeground(AZUL_TEXTO);
            } else if (falta) {
                lblAvisoInsumos.setText("Estoque insuficiente: os valores em vermelho ficariam negativos.");
                lblAvisoInsumos.setForeground(VERMELHO);
            } else {
                lblAvisoInsumos.setText("Estoque suficiente para esta produção.");
                lblAvisoInsumos.setForeground(new Color(46, 125, 50));
            }
            revalidate();
            return !falta && quantidade > 0;
        }

        private void salvar() {
            Receita r = (Receita) cbReceita.getSelectedItem();
            int quantidade = lerQuantidade();
            String lote = txtLote.getText().trim();
            LocalDate validade;
            try {
                validade = LocalDate.parse(txtValidade.getText(), FORMATO_DATA);
            } catch (DateTimeParseException e) {
                validade = null;
            }

            if (cbProduto.getSelectedIndex() <= 0 || r == null) {
                aviso("Selecione o produto e a receita.");
                return;
            }
            if (quantidade <= 0) {
                aviso("Digite uma quantidade produzida válida (número inteiro maior que zero).");
                return;
            }
            if (lote.isEmpty()) {
                aviso("Digite o número do lote.");
                return;
            }
            if (validade == null || !validade.isAfter(LocalDate.now())) {
                aviso("Digite uma data de validade válida e posterior a hoje.");
                return;
            }
            if (!recalcularInsumos()) {
                aviso("Não há insumos suficientes para esta produção.");
                return;
            }

            try {
                dao.registrarProducao(r.idReceita, quantidade, lote, validade,
                        USUARIO_LOGADO, txtObservacoes.getText().trim());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar produção: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                carregarBanco();
                return;
            }

            JOptionPane.showMessageDialog(this, "Produção salva!\n" + quantidade + " un de " + r.produto
                    + " (lote " + lote + ").\nOs insumos foram descontados do estoque.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            mostrarLista();
        }

        private String sugerirLote() {
            int proximoId = 1;
            for (Producao p : producoes) proximoId = Math.max(proximoId, p.id + 1);
            return String.format("L%03d", proximoId);
        }
    }

    // DADOS REAIS DO MYSQL
    private void carregarBanco() {
        if (carregamentoEmAndamento) return;
        carregamentoEmAndamento = true;

        new SwingWorker<DadosBanco, Void>() {
            @Override
            protected DadosBanco doInBackground() throws Exception {
                DadosBanco dados = new DadosBanco();
                for (Object[] linha : dao.listarReceitasAtivas()) {
                    int id = ((Number) linha[0]).intValue();
                    int rendimento = ((Number) linha[5]).intValue();
                    Receita receita = new Receita(id, (String) linha[1], (String) linha[3],
                            ((Number) linha[4]).intValue(), rendimento);
                    for (Object[] insumo : dao.listarInsumosReceita(id)) {
                        String nome = (String) insumo[1];
                        receita.insumo(nome, ((BigDecimal) insumo[2]).doubleValue());
                        dados.unidades.put(nome, (String) insumo[3]);
                        dados.estoques.put(nome, ((BigDecimal) insumo[4]).doubleValue());
                    }
                    dados.receitas.add(receita);
                }
                for (Object[] d : dao.listarProducoes()) {
                    dados.producoes.add(new Producao(((Number) d[0]).intValue(), (LocalDate) d[1],
                            (String) d[2], ((Number) d[3]).intValue(), (String) d[4],
                            (String) d[5], (String) d[6], (String) d[7],
                            (LocalDate) d[8], (String) d[9]));
                }
                return dados;
            }

            @Override
            protected void done() {
                carregamentoEmAndamento = false;
                try {
                    DadosBanco dados = get();
                    estoqueInsumos.clear();
                    estoqueInsumos.putAll(dados.estoques);
                    unidadeInsumos.clear();
                    unidadeInsumos.putAll(dados.unidades);
                    receitas.clear();
                    receitas.addAll(dados.receitas);
                    producoes.clear();
                    producoes.addAll(dados.producoes);
                    telaLista.atualizar();
                    if ("formulario".equals(telaAtual)) {
                        // Atualiza produtos/receitas disponíveis com os dados reais.
                        telaFormulario.limpar();
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(paginaProducao.this,
                            "Erro ao carregar produção do banco: " + causa.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static class DadosBanco {
        final Map<String, Double> estoques = new LinkedHashMap<>();
        final Map<String, String> unidades = new LinkedHashMap<>();
        final List<Receita> receitas = new ArrayList<>();
        final List<Producao> producoes = new ArrayList<>();
    }

    // COMPONENTES VISUAIS
    private JScrollPane rolavel(JComponent tela) {
        JPanel envoltorio = new PainelLarguraJanela();
        envoltorio.setLayout(new BorderLayout());
        envoltorio.setBackground(BRANCO);
        envoltorio.add(tela, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(null);
        scroll.setBackground(BRANCO);
        scroll.getViewport().setBackground(BRANCO);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static JComponent cabecalho(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.BOLD, 32));
        t.setForeground(AZUL_TITULO);
        JLabel s = new JLabel(subtitulo);
        s.setFont(new Font(FONTE, Font.PLAIN, 15));
        s.setForeground(AZUL_TEXTO);
        p.add(t);
        p.add(s);
        return p;
    }

    private static JComponent tituloSecao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FONTE, Font.BOLD, 14));
        l.setForeground(AZUL_TITULO);
        l.setOpaque(true);
        l.setBackground(FUNDO_SECAO);
        l.setBorder(new EmptyBorder(9, 10, 9, 10));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return l;
    }

    private static JPanel campo(String rotulo, JComponent componente, boolean obrigatorio) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(obrigatorio
                ? "<html>" + rotulo + " <font color='#d32f2f'>*</font></html>"
                : rotulo);
        l.setFont(new Font(FONTE, Font.PLAIN, obrigatorio ? 14 : 12));
        l.setForeground(AZUL_TITULO);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private static JPanel gradeCampos(double[] pesos, JComponent... campos) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        for (int i = 0; i < campos.length; i++) {
            g.gridx = i;
            g.weightx = pesos[i];
            g.insets = new Insets(0, i == 0 ? 4 : 0, 0, i < campos.length - 1 ? 20 : 4);
            p.add(campos[i], g);
        }
        return p;
    }

    private static JFormattedTextField criarCampoData() {
        JFormattedTextField campo;
        try {
            MaskFormatter mascara = new MaskFormatter("##/##/####");
            mascara.setPlaceholderCharacter('_');
            campo = new JFormattedTextField(mascara);
        } catch (ParseException e) {
            campo = new JFormattedTextField();
        }
        campo.setFont(new Font(FONTE, Font.PLAIN, 14));
        campo.setForeground(AZUL_TITULO);
        campo.setPreferredSize(new Dimension(150, 38));
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(0, 12, 0, 12)));
        return campo;
    }

    private static void recarregarCombo(JComboBox<String> combo, String primeiro, TreeSet<String> opcoes) {
        Object escolhida = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(primeiro);
        for (String o : opcoes) {
            combo.addItem(o);
        }
        combo.setSelectedItem(escolhida == null ? primeiro : escolhida);
        if (combo.getSelectedIndex() < 0) {
            combo.setSelectedIndex(0);
        }
    }

    private static void estilizarCombo(JComboBox<?> combo, int largura) {
        combo.setFont(new Font(FONTE, Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(largura, 38));
        combo.setBackground(BRANCO);
    }

    private static JButton botaoVerde(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font(FONTE, Font.PLAIN, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pintarBotao(b, VERDE, BRANCO, VERDE);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE, 1, true), new EmptyBorder(11, 26, 11, 26)));
        return b;
    }

    /** No Windows o JButton ignora setBackground; desligar o contentArea resolve. */
    private static void pintarBotao(JButton b, Color fundo, Color texto, Color borda) {
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(fundo);
        b.setForeground(texto);
        b.setBorder(new LineBorder(borda, 1, true));
    }

    private static void aviso(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private static String numero(double v) {
        return (v == Math.floor(v))
                ? String.format(BR, "%,.0f", v)      // 1150.0 -> "1.150"
                : String.format(BR, "%,.1f", v);     // 22.5 -> "22,5"
    }

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static class AoDigitar implements DocumentListener {
        private final Runnable acao;

        AoDigitar(Runnable acao) {
            this.acao = acao;
        }

        @Override public void insertUpdate(DocumentEvent e) { acao.run(); }
        @Override public void removeUpdate(DocumentEvent e) { acao.run(); }
        @Override public void changedUpdate(DocumentEvent e) { acao.run(); }
    }

    /** Painel que sempre ocupa a largura da janela (só rola na vertical). */
    private static class PainelLarguraJanela extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientacao, int direcao) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientacao, int direcao) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return true; }
    }

    /** Campo de texto com dica cinza quando está vazio. */
    private static class CampoTexto extends JTextField {
        private final String dica;

        CampoTexto(String dica) {
            this.dica = dica;
            setFont(new Font(FONTE, Font.PLAIN, 14));
            setForeground(AZUL_TITULO);
            setPreferredSize(new Dimension(150, 38));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(0, 12, 0, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = suavizar(g);
                g2.setFont(getFont());
                g2.setColor(new Color(160, 170, 190));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(dica, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        }
    }

    // =====================================================================
    // TABELA DA LISTA (selos, "Ver" e paginação)
    // =====================================================================
    private static class Selo extends JLabel {
        private Color corFundo = BRANCO;
        private Color corSelo = Color.LIGHT_GRAY;

        Selo() {
            setHorizontalAlignment(CENTER);
            setFont(new Font(FONTE, Font.PLAIN, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(corFundo);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 24;
            int h = 22;
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
            super.getTableCellRendererComponent(t, valor, selecionado, false, linha, coluna);
            Color fundo = selecionado ? VERDE_CLARO : (linha % 2 == 0 ? BRANCO : new Color(250, 251, 250));
            String nomeColuna = t.getColumnName(coluna);
            String texto = (valor == null) ? "" : valor.toString();

            if (nomeColuna.equals("Status")) {
                selo.setText(texto);
                selo.corFundo = fundo;
                if (CONCLUIDA.equals(texto)) {
                    selo.corSelo = new Color(220, 240, 220);
                    selo.setForeground(new Color(30, 110, 50));
                } else {
                    selo.corSelo = new Color(255, 236, 205);
                    selo.setForeground(new Color(200, 110, 0));
                }
                return selo;
            }
            setBackground(fundo);
            setFont(new Font(FONTE, Font.PLAIN, 13));
            setForeground(AZUL_TITULO);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setHorizontalAlignment(LEFT);
            if (nomeColuna.equals("Ações")) {
                setForeground(VERDE);
                setFont(new Font(FONTE, Font.BOLD, 13));
                setHorizontalAlignment(CENTER);
            }
            return this;
        }
    }

    private interface AcaoTabela {
        void executar(Object objeto);
    }

    private static class TabelaPaginada extends JPanel {
        private final String[] colunas;
        private final String nomeItens;
        private final DefaultTableModel modelo;
        private final JTable tabela;
        private final JLabel lblInfo = new JLabel();
        private final JPanel painelPaginas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private List<Object[]> linhas = new ArrayList<>();
        private List<?> objetos = new ArrayList<>();
        private int pagina = 0;
        private int porPagina = 10;

        TabelaPaginada(String[] colunas, String nomeItens, AcaoTabela acao) {
            this.colunas = colunas;
            this.nomeItens = nomeItens;

            modelo = new DefaultTableModel(colunas, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
            tabela = new JTable(modelo);
            tabela.setRowHeight(35);
            tabela.setGridColor(BORDA);
            tabela.setSelectionBackground(VERDE_CLARO);
            tabela.setDefaultRenderer(Object.class, new Renderizador());
            estilizarCabecalho(tabela);

            TableColumn colunaAcoes = tabela.getColumnModel().getColumn(colunas.length - 1);
            colunaAcoes.setMaxWidth(80);

            tabela.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    int coluna = tabela.columnAtPoint(e.getPoint());
                    int indice = pagina * porPagina + linha;
                    if (linha >= 0 && coluna == colunas.length - 1 && indice < objetos.size()) {
                        acao.executar(objetos.get(indice));
                    }
                }
            });

            JPanel caixa = new JPanel(new BorderLayout());
            caixa.setBorder(new LineBorder(BORDA, 1, true));
            caixa.add(tabela.getTableHeader(), BorderLayout.NORTH);
            caixa.add(tabela, BorderLayout.CENTER);

            lblInfo.setFont(new Font(FONTE, Font.PLAIN, 13));
            lblInfo.setForeground(AZUL_TEXTO);

            JComboBox<String> cbPorPagina = new JComboBox<>(new String[]{"10 por página", "20 por página", "50 por página"});
            cbPorPagina.setFont(new Font(FONTE, Font.PLAIN, 13));
            cbPorPagina.setBackground(BRANCO);
            cbPorPagina.addActionListener(e -> {
                porPagina = new int[]{10, 20, 50}[cbPorPagina.getSelectedIndex()];
                pagina = 0;
                atualizarPagina();
            });

            painelPaginas.setOpaque(false);
            JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            direita.setOpaque(false);
            direita.add(painelPaginas);
            direita.add(cbPorPagina);

            JPanel rodape = new JPanel(new BorderLayout());
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(16, 8, 0, 0));
            rodape.add(lblInfo, BorderLayout.WEST);
            rodape.add(direita, BorderLayout.EAST);

            setLayout(new BorderLayout());
            setOpaque(false);
            add(caixa, BorderLayout.CENTER);
            add(rodape, BorderLayout.SOUTH);
        }

        void larguras(int... valores) {
            for (int i = 0; i < valores.length && i < colunas.length; i++) {
                tabela.getColumnModel().getColumn(i).setPreferredWidth(valores[i]);
            }
        }

        void setLinhas(List<Object[]> novasLinhas, List<?> novosObjetos) {
            this.linhas = novasLinhas;
            this.objetos = novosObjetos;
            atualizarPagina();
        }

        void voltarParaPrimeiraPagina() {
            pagina = 0;
        }

        private void irPara(int novaPagina) {
            pagina = novaPagina;
            atualizarPagina();
        }

        private void atualizarPagina() {
            modelo.setRowCount(0);
            int total = linhas.size();
            int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) porPagina));
            if (pagina >= totalPaginas) {
                pagina = totalPaginas - 1;
            }
            int inicio = pagina * porPagina;
            int fim = Math.min(inicio + porPagina, total);
            for (int i = inicio; i < fim; i++) {
                modelo.addRow(linhas.get(i));
            }
            lblInfo.setText(total == 0
                    ? "Nenhuma produção encontrada."
                    : "Mostrando " + (inicio + 1) + " a " + fim + " de " + total + " " + nomeItens);

            painelPaginas.removeAll();
            painelPaginas.add(botaoPagina("<", false, pagina > 0, () -> irPara(pagina - 1)));
            int primeira = Math.max(0, pagina - 2);
            int ultima = Math.min(totalPaginas, primeira + 5);
            primeira = Math.max(0, ultima - 5);
            for (int p = primeira; p < ultima; p++) {
                final int destino = p;
                painelPaginas.add(botaoPagina(String.valueOf(p + 1), p == pagina, true, () -> irPara(destino)));
            }
            painelPaginas.add(botaoPagina(">", false, pagina < totalPaginas - 1, () -> irPara(pagina + 1)));
            revalidate();
            repaint();
        }

        private JButton botaoPagina(String texto, boolean ativo, boolean habilitado, Runnable aoClicar) {
            JButton b = new JButton(texto);
            b.setFont(new Font(FONTE, Font.PLAIN, 13));
            b.setFocusPainted(false);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setPreferredSize(new Dimension(36, 34));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(b, ativo ? VERDE : BRANCO, ativo ? BRANCO : AZUL_TITULO, ativo ? VERDE : BORDA);
            b.setEnabled(habilitado);
            b.addActionListener(e -> aoClicar.run());
            return b;
        }
    }

    private static void estilizarCabecalho(JTable tabela) {
        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 36));
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setFont(new Font(FONTE, Font.BOLD, 12));
                setBackground(FUNDO_SECAO);
                setForeground(AZUL_TITULO);
                String texto = String.valueOf(valor);
                setHorizontalAlignment(texto.equals("Ações") || texto.equals("Status") ? CENTER : LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, BORDA), new EmptyBorder(0, 12, 0, 8)));
                return this;
            }
        });
    }
}
