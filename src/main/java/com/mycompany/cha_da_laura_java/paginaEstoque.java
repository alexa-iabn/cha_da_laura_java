package com.mycompany.cha_da_laura_java;

import java.awt.*;
import com.mycompany.cha_da_laura_java.dao.EstoqueDAO;
import java.sql.SQLException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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


public class paginaEstoque extends JPanel {

    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color VERDE_SUBIU = new Color(46, 125, 50);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color VERMELHO = new Color(211, 47, 47);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color BRANCO = Color.WHITE;
    private static final Color FUNDO_VERMELHO = new Color(253, 236, 236);
    private static final Color FUNDO_AZUL = new Color(234, 242, 253);
    private static final Color FUNDO_ROXO = new Color(243, 238, 253);
    private static final String FONTE = "Segoe UI";

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Situações do estoque
    private static final String EM_ESTOQUE = "Em estoque";
    private static final String ESTOQUE_BAIXO = "Estoque baixo";
    private static final String SEM_ESTOQUE = "Sem estoque";

    private static final String ACOES_EDITAR = "editar/excluir"; // marca a célula com 2 ações
    private static final Set<String> COLUNAS_CENTRO = new HashSet<>(Arrays.asList(
            "Estoque atual", "Estoque mínimo", "Anterior", "Atual", "Qtd.", "Ações"));

    // TODO: trocar pelo nome do usuário que fez login
    private static final String USUARIO_LOGADO = "Maria";

    private static boolean MODO_CONSULTA = false;

    // ===== Dados carregados do MySQL =====
    private final List<Item> insumos = new ArrayList<>();
    private final List<Item> produtos = new ArrayList<>();
    private final List<Movimentacao> movimentacoes = new ArrayList<>();
    private int proximoIdMovimentacao = 1;
    

    // ===== Abas =====
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel painelAbas = new PainelLarguraJanela(cardLayout);
    private final String[] nomesAbas = {"Insumos", "Produtos", "Movimentação"};
    private final JButton[] botoesAbas = new JButton[nomesAbas.length];
    private final AbaItens abaInsumos;
    private final AbaItens abaProdutos;
    private final AbaMovimentacao abaMovimentacao;

    public paginaEstoque() {
        // Tradução dos botões do JOptionPane para português
        javax.swing.UIManager.put("OptionPane.yesButtonText", "Sim");
        javax.swing.UIManager.put("OptionPane.noButtonText", "Não");
        javax.swing.UIManager.put("OptionPane.okButtonText", "Confirmar");
        javax.swing.UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        // Dados carregados do MySQL em segundo plano; sem exemplos locais.

        setLayout(new BorderLayout());
        setBackground(BRANCO);
        setBorder(new EmptyBorder(20, 30, 20, 30));
        add(criarCabecalho(), BorderLayout.NORTH);

        abaInsumos = new AbaItens(insumos, false);
        abaProdutos = new AbaItens(produtos, true);
        abaMovimentacao = new AbaMovimentacao();

        painelAbas.setOpaque(false);
        painelAbas.add(empilhar(abaInsumos), nomesAbas[0]);
        painelAbas.add(empilhar(abaProdutos), nomesAbas[1]);
        painelAbas.add(empilhar(abaMovimentacao), nomesAbas[2]);

        JScrollPane scroll = new JScrollPane(painelAbas);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(BRANCO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        selecionarAba(0);
        carregarEstoqueBanco();
    }

    private void carregarEstoqueBanco() {
        new SwingWorker<List<Object[]>[], Void>() {
            @Override
            protected List<Object[]>[] doInBackground() throws Exception {
                EstoqueDAO dao = new EstoqueDAO();
                @SuppressWarnings("unchecked")
                List<Object[]>[] resultado = new List[3];
                resultado[0] = dao.listarInsumos();
                resultado[1] = dao.listarProdutos();
                resultado[2] = dao.listarMovimentacoes();
                return resultado;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]>[] dados = get();
                    insumos.clear();
                    produtos.clear();
                    for (Object[] d : dados[0]) {
                        insumos.add(new Item(((Number) d[0]).intValue(),
                                String.valueOf(d[1]), String.valueOf(d[2]),
                                String.valueOf(d[3]), ((Number) d[4]).doubleValue(),
                                ((Number) d[5]).doubleValue(), false));
                    }
                    for (Object[] d : dados[1]) {
                        produtos.add(new Item(((Number) d[0]).intValue(),
                                String.valueOf(d[1]), String.valueOf(d[2]),
                                String.valueOf(d[3]), ((Number) d[4]).doubleValue(),
                                ((Number) d[5]).doubleValue(), true));
                    }
                    movimentacoes.clear();
                    for (Object[] d : dados[2]) {
                        movimentacoes.add(new Movimentacao(((Number)d[0]).intValue(),
                                (LocalDateTime)d[1], String.valueOf(d[2]), String.valueOf(d[3]),
                                String.valueOf(d[4]), ((Number)d[5]).doubleValue(),
                                String.valueOf(d[6]), ((Number)d[7]).doubleValue(),
                                ((Number)d[8]).doubleValue(), String.valueOf(d[9]), String.valueOf(d[10])));
                    }
                    atualizarTudo();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(paginaEstoque.this,
                            "Erro ao carregar estoque do MySQL: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void atualizarTudo() {
        abaInsumos.atualizar();
        abaProdutos.atualizar();
        abaMovimentacao.atualizar();
    }

    
    // CABEÇALHO + BOTÕES DAS ABAS
    
    private JPanel criarCabecalho() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel titulo = new JLabel("Estoque");
        titulo.setFont(new Font(FONTE, Font.BOLD, 32));
        titulo.setForeground(AZUL_TITULO);

        JLabel subtitulo = new JLabel("Gerencie insumos, produtos e acompanhe as movimentações de estoque.");
        subtitulo.setFont(new Font(FONTE, Font.PLAIN, 15));
        subtitulo.setForeground(AZUL_TEXTO);

        JPanel abas = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        abas.setOpaque(false);
        abas.setBorder(new MatteBorder(0, 0, 1, 0, BORDA));
        for (int i = 0; i < nomesAbas.length; i++) {
            final int indice = i;
            JButton b = new JButton(nomesAbas[i]);
            b.setFont(new Font(FONTE, Font.PLAIN, 15));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(150, 42));
            b.addActionListener(e -> selecionarAba(indice));
            botoesAbas[i] = b;
            abas.add(b);
        }

        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        abas.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titulo);
        p.add(subtitulo);
        p.add(Box.createVerticalStrut(18));
        p.add(abas);
        p.add(Box.createVerticalStrut(18));
        return p;
    }

    private void selecionarAba(int indice) {
        cardLayout.show(painelAbas, nomesAbas[indice]);
        for (int i = 0; i < botoesAbas.length; i++) {
            boolean ativo = (i == indice);
            pintarBotao(botoesAbas[i],
                    ativo ? VERDE : new Color(244, 246, 244),
                    ativo ? BRANCO : AZUL_TITULO,
                    ativo ? VERDE : BORDA);
        }
    }

    // ABA "INSUMOS" e ABA "PRODUTOS" (mesma classe, muda só o tipo)
    private class AbaItens extends JPanel {
        private final List<Item> lista;
        private final boolean produto;
        private final String singular;
        private final String plural;
        private final KpiCard kpiTotal;
        private final KpiCard kpiBaixo;
        private final KpiCard kpiSem;
        private final KpiCard kpiOk;
        private final CampoBusca busca;
        private final JComboBox<String> cbCategoria = new JComboBox<>();
        private final JComboBox<String> cbSituacao =
                new JComboBox<>(new String[]{"Todas", EM_ESTOQUE, ESTOQUE_BAIXO, SEM_ESTOQUE});
        private final TabelaPaginada tabela;
        private boolean carregandoCombos = false;

        AbaItens(List<Item> lista, boolean produto) {
            this.lista = lista;
            this.produto = produto;
            this.singular = produto ? "produto" : "insumo";
            this.plural = produto ? "produtos" : "insumos";
            String rotuloMedida = produto ? "Peso/Volume" : "Unidade de medida";

            kpiTotal = new KpiCard("Total de " + plural, null, BRANCO, AZUL_TITULO);
            kpiBaixo = new KpiCard("Abaixo do estoque mínimo", null, FUNDO_VERMELHO, VERMELHO);
            kpiSem = new KpiCard("Sem estoque", null, FUNDO_AZUL, AZUL_TITULO);
            kpiOk = new KpiCard("Em estoque adequado", null, BRANCO, AZUL_TITULO);

            JPanel linhaKpis = new JPanel(new GridLayout(1, 4, 12, 0));
            linhaKpis.setOpaque(false);
            linhaKpis.add(kpiTotal);
            linhaKpis.add(kpiBaixo);
            linhaKpis.add(kpiSem);
            linhaKpis.add(kpiOk);

            // --- Filtros ---
            busca = new CampoBusca("Pesquisar por nome ou categoria...");
            busca.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { filtrosMudaram(); }
                @Override public void removeUpdate(DocumentEvent e) { filtrosMudaram(); }
                @Override public void changedUpdate(DocumentEvent e) { filtrosMudaram(); }
            });
            estilizarCombo(cbCategoria, 180);
            estilizarCombo(cbSituacao, 180);
            cbCategoria.addActionListener(e -> {
                if (!carregandoCombos) {
                    filtrosMudaram();
                }
            });
            cbSituacao.addActionListener(e -> filtrosMudaram());

            JButton btNovo = botaoVerde("+  Novo " + singular);
            btNovo.addActionListener(e -> abrirFormulario(null));

            JPanel filtros = new JPanel(new GridBagLayout());
            filtros.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = 0;
            g.anchor = GridBagConstraints.SOUTH;
            g.insets = new Insets(0, 0, 0, 12);
            g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            filtros.add(busca, g);
            g.gridx = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            filtros.add(campo("Categoria", cbCategoria), g);
            g.gridx = 2;
            filtros.add(campo("Situação", cbSituacao), g);
            g.gridx = 3; g.insets = new Insets(0, 0, 0, 0);
            filtros.add(btNovo, g);

            // --- Tabela ---
            tabela = new TabelaPaginada(
                new String[]{"ID", "Nome do " + singular, "Categoria", rotuloMedida,
                    "Estoque atual", "Estoque mínimo", "Situação", "Ações"},
                plural,
                (objeto, ladoDireito) -> {
                    Item item = (Item) objeto;
                    if (ladoDireito) {
                        excluir(item);
                    } else {
                        abrirFormulario(item);
                    }
                });
            tabela.larguras(40, 200, 120, 120, 100, 100, 130, 140);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            JComponent[] partes = {linhaKpis, filtros, tabela};
            for (JComponent parte : partes) {
                parte.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(parte);
                add(Box.createVerticalStrut(16));
            }

            atualizar();
        }

        private void filtrosMudaram() {
            tabela.voltarParaPrimeiraPagina();
            atualizar();
        }

        void atualizar() {
            // 1) Números dos cards
            int baixo = 0;
            int sem = 0;
            int ok = 0;
            for (Item i : lista) {
                switch (i.situacao()) {
                    case SEM_ESTOQUE: sem++; break;
                    case ESTOQUE_BAIXO: baixo++; break;
                    default: ok++;
                }
            }
            kpiTotal.setValor(lista.size());
            kpiBaixo.setValor(baixo);
            kpiSem.setValor(sem);
            kpiOk.setValor(ok);

            // 2) Lista de categorias (sem perder a que estava escolhida)
            carregandoCombos = true;
            Object escolhida = cbCategoria.getSelectedItem();
            cbCategoria.removeAllItems();
            cbCategoria.addItem("Todas");
            for (String c : categorias(lista)) {
                cbCategoria.addItem(c);
            }
            cbCategoria.setSelectedItem(escolhida == null ? "Todas" : escolhida);
            if (cbCategoria.getSelectedIndex() < 0) {
                cbCategoria.setSelectedIndex(0);
            }
            carregandoCombos = false;

            // 3) Filtrar as linhas da tabela
            String texto = busca.getText().trim().toLowerCase();
            String categoria = (String) cbCategoria.getSelectedItem();
            String situacao = (String) cbSituacao.getSelectedItem();

            List<Object[]> linhas = new ArrayList<>();
            List<Item> objetos = new ArrayList<>();
            for (Item i : lista) {
                if (!texto.isEmpty()
                        && !i.nome.toLowerCase().contains(texto)
                        && !i.categoria.toLowerCase().contains(texto)) {
                    continue;
                }
                if (!"Todas".equals(categoria) && !i.categoria.equals(categoria)) {
                    continue;
                }
                if (!"Todas".equals(situacao) && !i.situacao().equals(situacao)) {
                    continue;
                }
                linhas.add(new Object[]{i.id, i.nome, i.categoria, i.medida,
                    i.atual, i.minimo, i.situacao(), ACOES_EDITAR});
                objetos.add(i);
            }
            tabela.setLinhas(linhas, objetos);
        }

        /** Cadastro de insumos. Produtos permanecem em modo consulta neste passo. */
        private void abrirFormulario(Item item) {
            boolean novo = item == null;
            JTextField txtNome = new JTextField(novo ? "" : item.nome, 22);
            JComboBox<String> cbCat = new JComboBox<>(categorias(lista).toArray(new String[0]));
            cbCat.setEditable(true);
            cbCat.setSelectedItem(novo ? "" : item.categoria);
            JTextField txtMedida = new JTextField(novo ? "kg" : item.medida);
            JSpinner spMinimo = new JSpinner(new SpinnerNumberModel(novo ? 10.0 : item.minimo, 0.0, 1000000.0, 1.0));
            JSpinner spInicial = novo
                    ? new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 1.0))
                    : null;
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
            form.add(new JLabel(produto ? "Nome do produto *" : "Nome do insumo *")); form.add(txtNome);
            form.add(new JLabel("Categoria *")); form.add(cbCat);
            form.add(new JLabel(produto ? "Peso/Volume *" : "Unidade de medida *")); form.add(txtMedida);
            form.add(new JLabel("Estoque mínimo")); form.add(spMinimo);
            if (novo) {
                form.add(new JLabel("Estoque inicial *"));
                form.add(spInicial);
            } else {
                form.add(new JLabel("Estoque atual: alterações somente por Movimentação"));
            }
            while (true) {
                int resposta = JOptionPane.showConfirmDialog(this, form,
                        novo ? "Novo " + singular : "Editar " + singular, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (resposta != JOptionPane.OK_OPTION) return;
                String nome = txtNome.getText().trim();
                String categoria = String.valueOf(cbCat.getSelectedItem() == null ? "" : cbCat.getSelectedItem()).trim();
                String unidade = txtMedida.getText().trim();
                double minimo = ((Number) spMinimo.getValue()).doubleValue();
                double inicial = novo ? ((Number) spInicial.getValue()).doubleValue() : 0.0;
                if (nome.isEmpty() || categoria.isEmpty() || unidade.isEmpty()) {
                    aviso("Preencha nome, categoria e unidade de medida."); continue;
                }
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() throws Exception {
                        EstoqueDAO dao = new EstoqueDAO();
                        if (produto) {
                            if (novo) dao.inserirProduto(nome, categoria, unidade, minimo, inicial);
                            else dao.atualizarProduto(item.id, nome, categoria, unidade, minimo);
                        } else {
                            if (novo) dao.inserirInsumo(nome, categoria, unidade, minimo, inicial);
                            else dao.atualizarInsumo(item.id, nome, categoria, unidade, minimo);
                        }
                        return null;
                    }
                    @Override protected void done() {
                        try {
                            get();
                            carregarEstoqueBanco();
                            JOptionPane.showMessageDialog(paginaEstoque.this,
                                    novo ? "Cadastro realizado com sucesso!" : "Item atualizado com sucesso!");
                        } catch (Exception ex) {
                            Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                            aviso("Erro ao salvar insumo: " + causa.getMessage());
                        }
                    }
                }.execute();
                return;
            }
        }

        private void excluir(Item item) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Desativar o insumo \"" + item.nome + "\"? Ele deixará de aparecer no Estoque.",
                    "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (resposta != JOptionPane.YES_OPTION) return;
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    if (produto) new EstoqueDAO().excluirProduto(item.id);
                    else new EstoqueDAO().excluirInsumo(item.id);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get(); carregarEstoqueBanco();
                        JOptionPane.showMessageDialog(paginaEstoque.this, "Insumo desativado com sucesso!");
                    } catch (Exception ex) {
                        Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                        aviso("Erro ao excluir insumo: " + causa.getMessage());
                    }
                }
            }.execute();
        }
    }

    // =====================================================================
    // ABA "MOVIMENTAÇÃO"
    // =====================================================================
    private class AbaMovimentacao extends JPanel {
        private final KpiCard kpiEntradas = new KpiCard("Entradas", "Total de itens que entraram", FUNDO_AZUL, AZUL_TITULO);
        private final KpiCard kpiSaidas = new KpiCard("Saídas", "Total de itens que saíram", FUNDO_VERMELHO, VERMELHO);
        private final KpiCard kpiAjustes = new KpiCard("Ajustes", "Ajustes manuais", FUNDO_ROXO, AZUL_TITULO);
        private final KpiCard kpiTotal = new KpiCard("Total de movimentações", "No período filtrado", BRANCO, AZUL_TITULO);

        private final JComboBox<String> cbPeriodo = new JComboBox<>(new String[]{
            "Todo o período", "Hoje", "Últimos 7 dias", "Últimos 30 dias", "Este mês"});
        private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Todas", "Entrada", "Saída", "Ajuste"});
        private final JComboBox<String> cbItem = new JComboBox<>();
        private final JComboBox<String> cbUsuario = new JComboBox<>();
        private final TabelaPaginada tabela;
        private boolean carregandoCombos = false;

        AbaMovimentacao() {
            JPanel linhaKpis = new JPanel(new GridLayout(1, 4, 12, 0));
            linhaKpis.setOpaque(false);
            linhaKpis.add(kpiEntradas);
            linhaKpis.add(kpiSaidas);
            linhaKpis.add(kpiAjustes);
            linhaKpis.add(kpiTotal);

            JComboBox<?>[] combos = {cbPeriodo, cbTipo, cbItem, cbUsuario};
            for (JComboBox<?> c : combos) {
                estilizarCombo(c, 160);
                c.addActionListener(e -> filtrosMudaram());
            }

            JButton btNova = botaoVerde("+  Nova movimentação");
            btNova.addActionListener(e -> novaMovimentacao());

            JPanel filtros = new JPanel(new GridBagLayout());
            filtros.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = 0;
            g.anchor = GridBagConstraints.SOUTH;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 0, 12);
            String[] rotulos = {"Período", "Tipo de movimentação", "Item", "Usuário"};
            for (int i = 0; i < combos.length; i++) {
                g.gridx = i;
                filtros.add(campo(rotulos[i], combos[i]), g);
            }
            g.gridx = combos.length;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            g.insets = new Insets(0, 0, 0, 0);
            filtros.add(btNova, g);

            tabela = new TabelaPaginada(
                new String[]{"ID", "Data e hora", "Tipo", "Item", "Categoria", "Qtd.",
                    "Anterior", "Atual", "Motivo", "Usuário", "Ações"},
                "movimentações",
                (objeto, ladoDireito) -> mostrarDetalhes((Movimentacao) objeto));
            tabela.larguras(35, 125, 80, 160, 95, 75, 70, 60, 145, 75, 60);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            JComponent[] partes = {linhaKpis, filtros, tabela};
            for (JComponent parte : partes) {
                parte.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(parte);
                add(Box.createVerticalStrut(16));
            }

            atualizar();
        }

        private void filtrosMudaram() {
            if (!carregandoCombos) {
                tabela.voltarParaPrimeiraPagina();
                atualizar();
            }
        }

        void atualizar() {
            // 1) Recarregar as opções de "Item" e "Usuário"
            carregandoCombos = true;
            TreeSet<String> itens = new TreeSet<>();
            TreeSet<String> usuarios = new TreeSet<>();
            for (Movimentacao m : movimentacoes) {
                itens.add(m.item);
                usuarios.add(m.usuario);
            }
            recarregarCombo(cbItem, "Todos os itens", itens);
            recarregarCombo(cbUsuario, "Todos os usuários", usuarios);
            carregandoCombos = false;

            // 2) Filtrar
            String periodo = (String) cbPeriodo.getSelectedItem();
            String tipo = (String) cbTipo.getSelectedItem();
            String item = (String) cbItem.getSelectedItem();
            String usuario = (String) cbUsuario.getSelectedItem();
            LocalDate hoje = LocalDate.now();

            List<Object[]> linhas = new ArrayList<>();
            List<Movimentacao> objetos = new ArrayList<>();
            int entradas = 0;
            int saidas = 0;
            int ajustes = 0;
            for (Movimentacao m : movimentacoes) {
                LocalDate dia = m.dataHora.toLocalDate();
                boolean noPeriodo;
                switch (periodo) {
                    case "Hoje": noPeriodo = dia.equals(hoje); break;
                    case "Últimos 7 dias": noPeriodo = !dia.isBefore(hoje.minusDays(6)); break;
                    case "Últimos 30 dias": noPeriodo = !dia.isBefore(hoje.minusDays(29)); break;
                    case "Este mês": noPeriodo = dia.getYear() == hoje.getYear() && dia.getMonth() == hoje.getMonth(); break;
                    default: noPeriodo = true;
                }
                if (!noPeriodo
                        || (!"Todas".equals(tipo) && !m.tipo.equals(tipo))
                        || (!"Todos os itens".equals(item) && !m.item.equals(item))
                        || (!"Todos os usuários".equals(usuario) && !m.usuario.equals(usuario))) {
                    continue;
                }
                switch (m.tipo) {
                    case "Entrada": entradas++; break;
                    case "Saída": saidas++; break;
                    default: ajustes++;
                }
                String qtd = (m.quantidade >= 0 ? "+ " : "- ") + numero(Math.abs(m.quantidade)) + " " + m.unidade;
                linhas.add(new Object[]{m.id, m.dataHora.format(DATA_HORA), m.tipo, m.item, m.categoria,
                    qtd, m.anterior, m.atual, m.motivo, m.usuario, "Ver"});
                objetos.add(m);
            }

            kpiEntradas.setValor(entradas);
            kpiSaidas.setValor(saidas);
            kpiAjustes.setValor(ajustes);
            kpiTotal.setValor(linhas.size());
            tabela.setLinhas(linhas, objetos);
        }

        private void recarregarCombo(JComboBox<String> combo, String primeiro, TreeSet<String> opcoes) {
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

        private void novaMovimentacao() {
            List<Item> todos = new ArrayList<>(insumos);
            todos.addAll(produtos);
            if (todos.isEmpty()) {
                aviso("Cadastre um insumo ou produto primeiro.");
                return;
            }

            JComboBox<String> cbTipoMov = new JComboBox<>(new String[]{"Entrada", "Saída", "Ajuste"});
            JComboBox<Item> cbItemMov = new JComboBox<>(todos.toArray(new Item[0]));
            JSpinner spQuantidade = new JSpinner(new SpinnerNumberModel(1.0, -1_000_000.0, 1_000_000.0, 1.0));
            JComboBox<String> cbMotivo = new JComboBox<>(new String[]{
                "Compra", "Venda (loja física)", "Venda (parceiro)",
                "Produção", "Ajuste de inventário", "Perda"});
            cbMotivo.setEditable(true);
            JTextField txtUsuario = new JTextField(USUARIO_LOGADO);

            JLabel dica = new JLabel("No ajuste, use número negativo para diminuir o estoque.");
            dica.setForeground(AZUL_TEXTO);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
            form.add(new JLabel("Tipo"));
            form.add(cbTipoMov);
            form.add(new JLabel("Item"));
            form.add(cbItemMov);
            form.add(new JLabel("Quantidade"));
            form.add(spQuantidade);
            form.add(dica);
            form.add(new JLabel("Motivo"));
            form.add(cbMotivo);
            form.add(new JLabel("Usuário"));
            form.add(txtUsuario);

            while (true) {
                int resposta = JOptionPane.showConfirmDialog(this, form, "Nova movimentação",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (resposta != JOptionPane.OK_OPTION) {
                    return;
                }
                String tipo = (String) cbTipoMov.getSelectedItem();
                Item item = (Item) cbItemMov.getSelectedItem();
                double quantidade = ((Number) spQuantidade.getValue()).doubleValue();
                String motivo = String.valueOf(cbMotivo.getSelectedItem() == null ? "" : cbMotivo.getSelectedItem()).trim();
                String usuario = txtUsuario.getText().trim();

                if (quantidade == 0) {
                    aviso("A quantidade não pode ser zero.");
                    continue;
                }
                if (!"Ajuste".equals(tipo) && quantidade < 0) {
                    aviso("Em entrada e saída use uma quantidade positiva.");
                    continue;
                }
                double variacao = "Saída".equals(tipo) ? -quantidade : quantidade;
                if (item.atual + variacao < 0) {
                    aviso("Estoque insuficiente. Estoque atual de " + item.nome + ": "
                            + numero(item.atual) + " " + item.unidade());
                    continue;
                }
                if (motivo.isEmpty() || usuario.isEmpty()) {
                    aviso("Preencha o motivo e o usuário.");
                    continue;
                }

                new SwingWorker<Double, Void>() {
                    @Override protected Double doInBackground() throws Exception {
                        return new EstoqueDAO().registrarMovimentacao(item.id,item.produto,tipo,quantidade,motivo,usuario);
                    }
                    @Override protected void done() {
                        try {
                            double novoEstoque=get();
                            carregarEstoqueBanco();
                            JOptionPane.showMessageDialog(paginaEstoque.this,
                                "Movimentação registrada!\nNovo estoque de " + item.nome + ": "
                                + numero(novoEstoque) + " " + item.unidade());
                        } catch(Exception ex) {
                            Throwable causa=ex.getCause()==null?ex:ex.getCause();
                            aviso("Erro na movimentação: " + causa.getMessage());
                        }
                    }
                }.execute();
                return;
            }
        }

        private void mostrarDetalhes(Movimentacao m) {
            String texto = "ID: " + m.id
                    + "\nData e hora: " + m.dataHora.format(DATA_HORA)
                    + "\nTipo: " + m.tipo
                    + "\nItem: " + m.item + " (" + m.categoria + ")"
                    + "\nQuantidade: " + (m.quantidade >= 0 ? "+" : "-") + numero(Math.abs(m.quantidade)) + " " + m.unidade
                    + "\nEstoque: " + numero(m.anterior) + " → " + numero(m.atual)
                    + "\nMotivo: " + m.motivo
                    + "\nUsuário: " + m.usuario;
            JOptionPane.showMessageDialog(this, texto, "Detalhes da movimentação", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // MODELOS 
    private static class Item {
        int id;
        String nome;
        String categoria;
        String medida;    // insumo: unidade (kg, ml) | produto: peso/volume (30 g)
        double atual;
        double minimo;
        boolean produto;

        Item(int id, String nome, String categoria, String medida, double atual, double minimo, boolean produto) {
            this.id = id;
            this.nome = nome;
            this.categoria = categoria;
            this.medida = medida;
            this.atual = atual;
            this.minimo = minimo;
            this.produto = produto;
        }

        String situacao() {
            if (atual <= 0) {
                return SEM_ESTOQUE;
            }
            return atual < minimo ? ESTOQUE_BAIXO : EM_ESTOQUE;
        }

        String unidade() {
            return produto ? "un" : medida;
        }

        @Override
        public String toString() { // texto que aparece no combo "Item"
            return nome + (produto ? " (produto)" : " (insumo)")
                    + " - estoque: " + numero(atual) + " " + unidade();
        }
    }

    private static class Movimentacao {
        final int id;
        final LocalDateTime dataHora;
        final String tipo;
        final String item;
        final String categoria;
        final double quantidade; // positiva = entrou, negativa = saiu
        final String unidade;
        final double anterior;
        final double atual;
        final String motivo;
        final String usuario;

        Movimentacao(int id, LocalDateTime dataHora, String tipo, String item, String categoria,
                double quantidade, String unidade, double anterior, double atual, String motivo, String usuario) {
            this.id = id;
            this.dataHora = dataHora;
            this.tipo = tipo;
            this.item = item;
            this.categoria = categoria;
            this.quantidade = quantidade;
            this.unidade = unidade;
            this.anterior = anterior;
            this.atual = atual;
            this.motivo = motivo;
            this.usuario = usuario;
        }
    }

    private static int proximoId(List<Item> lista) {
        int maior = 0;
        for (Item i : lista) {
            maior = Math.max(maior, i.id);
        }
        return maior + 1;
    }

    private static TreeSet<String> categorias(List<Item> lista) {
        TreeSet<String> cats = new TreeSet<>();
        for (Item i : lista) {
            cats.add(i.categoria);
        }
        return cats;
    }

    // =====================================================================
    // DADOS DE EXEMPLO (iguais ao protótipo)
    // TODO: apagar e carregar do banco com os DAOs
    // =====================================================================
    private void carregarDadosExemplo() {
        Object[][] ins = {
            {"Camomila", "Ervas", "kg", 5, 10}, {"Hortelã", "Ervas", "kg", 7, 10},
            {"Erva-doce", "Ervas", "kg", 12, 10}, {"Hibisco", "Ervas", "kg", 0, 10},
            {"Chá verde", "Ervas", "kg", 15, 10}, {"Gengibre", "Especiarias", "kg", 8, 10},
            {"Canela", "Especiarias", "kg", 20, 10}, {"Cravo", "Especiarias", "kg", 3, 10},
            {"Cardamomo", "Especiarias", "kg", 0, 10}, {"Baunilha", "Especiarias", "ml", 25, 10},
            {"Lavanda", "Ervas", "kg", 14, 10}, {"Melissa", "Ervas", "kg", 11, 10},
            {"Capim-limão", "Ervas", "kg", 18, 10}, {"Alecrim", "Ervas", "kg", 6, 10},
            {"Rooibos", "Ervas", "kg", 22, 10}, {"Folha de amora", "Ervas", "kg", 13, 10},
            {"Chá preto", "Ervas", "kg", 30, 10}, {"Maçã desidratada", "Frutas", "kg", 16, 10},
            {"Pimenta-rosa", "Especiarias", "kg", 12, 5}, {"Anis-estrelado", "Especiarias", "kg", 9, 5}
        };
        for (int i = 0; i < ins.length; i++) {
            insumos.add(new Item(i + 1, (String) ins[i][0], (String) ins[i][1], (String) ins[i][2],
                    (Integer) ins[i][3], (Integer) ins[i][4], false));
        }

        Object[][] prods = {
            {"Blend Calme", "Blends", "30 g", 25, 10}, {"Blend Felicità", "Blends", "80 g", 18, 10},
            {"Blend Ormoni", "Blends", "40 g", 6, 10}, {"Blend MaterniTea", "Blends", "90 g", 0, 10},
            {"Airmid", "Blends", "50 g", 12, 10}, {"Chai Masala", "Blends", "80 g", 8, 10},
            {"Home Spray Lavanda", "Home Spray", "100 ml", 15, 10},
            {"Home Spray Capim-limão", "Home Spray", "100 ml", 4, 10},
            {"Kit Relax", "Kits", "-", 10, 5}, {"Kit Energia", "Kits", "-", 2, 5},
            {"Blend Detox", "Blends", "40 g", 20, 10}, {"Blend Hibisco", "Blends", "50 g", 14, 10},
            {"Home Spray Alecrim", "Home Spray", "100 ml", 11, 10}, {"Kit Presente", "Kits", "-", 0, 5}
        };
        for (int i = 0; i < prods.length; i++) {
            produtos.add(new Item(i + 1, (String) prods[i][0], (String) prods[i][1], (String) prods[i][2],
                    (Integer) prods[i][3], (Integer) prods[i][4], true));
        }

        Object[][] movs = {
            {LocalDateTime.of(2026, 10, 10, 14, 32), "Entrada", "Camomila", "Ervas", 50, "kg", 5, 55, "Compra", "Maria"},
            {LocalDateTime.of(2026, 10, 9, 10, 15), "Saída", "Blend Calme 30g", "Blends", -10, "un", 30, 20, "Venda (loja física)", "João"},
            {LocalDateTime.of(2026, 10, 8, 16, 20), "Saída", "Hortelã", "Ervas", -2, "kg", 12, 10, "Produção", "Maria"},
            {LocalDateTime.of(2026, 10, 7, 9, 40), "Entrada", "Erva-doce", "Ervas", 20, "kg", 0, 20, "Compra", "Matheus"},
            {LocalDateTime.of(2026, 10, 6, 11, 25), "Ajuste", "Chá verde", "Ervas", 5, "kg", 10, 15, "Ajuste de inventário", "Maria"},
            {LocalDateTime.of(2026, 10, 5, 15, 10), "Saída", "Blend Felicità 80g", "Blends", -5, "un", 25, 20, "Venda (e-commerce)", "João"},
            {LocalDateTime.of(2026, 10, 4, 13, 50), "Saída", "Home Spray Lavanda", "Home Spray", -3, "un", 10, 7, "Venda (parceiro)", "Matheus"},
            {LocalDateTime.of(2026, 10, 3, 8, 20), "Entrada", "Canela", "Especiarias", 15, "kg", 5, 20, "Compra", "Maria"},
            {LocalDateTime.of(2026, 10, 2, 17, 5), "Saída", "Blend Ormoni 40g", "Blends", -8, "un", 18, 10, "Produção", "João"},
            {LocalDateTime.of(2026, 10, 1, 9, 12), "Ajuste", "Hibisco", "Ervas", -2, "kg", 2, 0, "Perda", "Matheus"}
        };
        for (Object[] m : movs) {
            movimentacoes.add(new Movimentacao(proximoIdMovimentacao++, (LocalDateTime) m[0], (String) m[1],
                    (String) m[2], (String) m[3], (Integer) m[4], (String) m[5],
                    (Integer) m[6], (Integer) m[7], (String) m[8], (String) m[9]));
        }
    }

    // =====================================================================
    // COMPONENTES VISUAIS
    // =====================================================================

    /** Card colorido com número grande. */
    private static class KpiCard extends JPanel {
        private final JLabel lblNumero = new JLabel("0");

        KpiCard(String titulo, String subtitulo, Color fundo, Color corNumero) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(fundo);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(fundo.equals(BRANCO) ? BORDA : fundo.darker(), 1, true),
                    new EmptyBorder(14, 20, 14, 20)));

            lblNumero.setFont(new Font(FONTE, Font.BOLD, 26));
            lblNumero.setForeground(corNumero);
            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(new Font(FONTE, Font.PLAIN, 13));
            lblTitulo.setForeground(corNumero.equals(VERMELHO) ? VERMELHO : AZUL_TITULO);

            add(lblNumero);
            add(lblTitulo);
            if (subtitulo != null) {
                JLabel lblSub = new JLabel(subtitulo);
                lblSub.setFont(new Font(FONTE, Font.PLAIN, 11));
                lblSub.setForeground(AZUL_TEXTO);
                add(lblSub);
            }
        }

        void setValor(int valor) {
            lblNumero.setText(String.valueOf(valor));
        }
    }

    private JPanel empilhar(JComponent conteudo) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(conteudo, BorderLayout.NORTH);
        return envoltorio;
    }

    private static JPanel campo(String rotulo, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(rotulo);
        l.setFont(new Font(FONTE, Font.PLAIN, 12));
        l.setForeground(AZUL_TITULO);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private static void estilizarCombo(JComboBox<?> combo, int largura) {
        combo.setFont(new Font(FONTE, Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(largura, 38));
        combo.setBackground(BRANCO);
    }

    private static JButton botaoVerde(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font(FONTE, Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pintarBotao(b, VERDE, BRANCO, VERDE);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE, 1, true), new EmptyBorder(9, 20, 9, 20)));
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
                ? String.format(BR, "%,.0f", v)      // 1250.0 -> "1.250"
                : String.format(BR, "%,.2f", v);     // 2.5 -> "2,50"
    }

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    /** Painel que sempre ocupa a largura da janela (só rola na vertical). */
    private static class PainelLarguraJanela extends JPanel implements Scrollable {
        PainelLarguraJanela(LayoutManager layout) {
            super(layout);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientacao, int direcao) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientacao, int direcao) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Campo de texto com dica cinza quando está vazio. */
    private static class CampoBusca extends JTextField {
        private final String dica;

        CampoBusca(String dica) {
            this.dica = dica;
            setFont(new Font(FONTE, Font.PLAIN, 14));
            setForeground(AZUL_TITULO);
            setPreferredSize(new Dimension(200, 38));
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
    // TABELA: selos coloridos, ações e paginação
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
            int w = fm.stringWidth(getText()) + 22;
            int h = 20;
            g2.setColor(corSelo);
            g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Renderizador extends DefaultTableCellRenderer {
        private final Selo selo = new Selo();
        private static final String TEXTO_ACOES =
                "<html><b><font color='#165728'>Editar</font>&nbsp;&nbsp;&nbsp;"
                + "<font color='#d32f2f'>Excluir</font></b></html>";

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(t, valor, selecionado, false, linha, coluna);
            Color fundo = selecionado ? VERDE_CLARO : (linha % 2 == 0 ? BRANCO : new Color(250, 251, 250));
            String nomeColuna = t.getColumnName(coluna);
            String texto = (valor == null) ? "" : valor.toString();

            if (nomeColuna.equals("Situação") || nomeColuna.equals("Tipo")) {
                selo.setText(texto);
                selo.corFundo = fundo;
                if (texto.equals(EM_ESTOQUE) || texto.equals("Entrada")) {
                    selo.corSelo = new Color(220, 240, 220);
                    selo.setForeground(new Color(30, 110, 50));
                } else if (texto.equals(ESTOQUE_BAIXO)) {
                    selo.corSelo = new Color(255, 236, 179);
                    selo.setForeground(new Color(140, 90, 0));
                } else if (texto.equals(SEM_ESTOQUE) || texto.equals("Saída")) {
                    selo.corSelo = new Color(253, 224, 226);
                    selo.setForeground(new Color(198, 40, 40));
                } else { // Ajuste
                    selo.corSelo = new Color(232, 224, 250);
                    selo.setForeground(new Color(94, 53, 177));
                }
                return selo;
            }
            setBackground(fundo);
            setFont(new Font(FONTE, Font.PLAIN, 13));
            setForeground(AZUL_TITULO);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(COLUNAS_CENTRO.contains(nomeColuna) ? CENTER : LEFT);
            if (valor instanceof Number) {
                setText(numero(((Number) valor).doubleValue()));
            }

            if (ACOES_EDITAR.equals(texto)) {          // metade esquerda = Editar, direita = Excluir
                setText(TEXTO_ACOES);
            } else if (nomeColuna.equals("Ações")) {   // "Ver"
                setForeground(VERDE);
                setFont(new Font(FONTE, Font.BOLD, 13));
            } else if (nomeColuna.equals("Qtd.")) {
                if (texto.startsWith("+")) {
                    setForeground(VERDE_SUBIU);
                } else if (texto.startsWith("-")) {
                    setForeground(VERMELHO);
                }
            } else if (nomeColuna.equals("Estoque atual") && valor instanceof Number) {
                // vermelho quando está abaixo do mínimo
                int colunaMinimo = ((DefaultTableModel) t.getModel()).findColumn("Estoque mínimo");
                if (colunaMinimo >= 0) {
                    Object minimo = t.getModel().getValueAt(linha, colunaMinimo);
                    if (minimo instanceof Number
                            && ((Number) valor).doubleValue() < ((Number) minimo).doubleValue()) {
                        setForeground(VERMELHO);
                    }
                }
            }
            return this;
        }
    }

    /** O que fazer quando clicam na coluna "Ações" (ladoDireito = clicou em "Excluir"). */
    private interface AcaoTabela {
        void executar(Object objeto, boolean ladoDireito);
    }

    private static class TabelaPaginada extends JPanel {
        private final String[] colunas;
        private final String nomeItens;
        private final AcaoTabela acao;
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
            this.acao = acao;

            modelo = new DefaultTableModel(colunas, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
            tabela = new JTable(modelo);
            tabela.setRowHeight(32);
            tabela.setGridColor(BORDA);
            tabela.setSelectionBackground(VERDE_CLARO);
            tabela.setDefaultRenderer(Object.class, new Renderizador());
            estilizarCabecalho(tabela);

            TableColumn colunaId = tabela.getColumnModel().getColumn(0);
            colunaId.setPreferredWidth(40);
            colunaId.setMaxWidth(50);
            TableColumn colunaAcoes = tabela.getColumnModel().getColumn(colunas.length - 1);
            colunaAcoes.setPreferredWidth(140);
            colunaAcoes.setMinWidth(55);
            colunaAcoes.setMaxWidth(160);

            tabela.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    int coluna = tabela.columnAtPoint(e.getPoint());
                    if (linha < 0 || coluna != colunas.length - 1) {
                        return;
                    }
                    Rectangle celula = tabela.getCellRect(linha, coluna, false);
                    boolean ladoDireito = e.getX() > celula.x + celula.width / 2;
                    int indice = pagina * porPagina + linha;
                    if (indice < objetos.size()) {
                        acao.executar(objetos.get(indice), ladoDireito);
                    }
                }
            });

            JPanel caixa = new JPanel(new BorderLayout());
            caixa.setBorder(new LineBorder(BORDA));
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
            rodape.setBorder(new EmptyBorder(12, 0, 0, 0));
            rodape.add(lblInfo, BorderLayout.WEST);
            rodape.add(direita, BorderLayout.EAST);

            setLayout(new BorderLayout());
            setOpaque(false);
            add(caixa, BorderLayout.CENTER);
            add(rodape, BorderLayout.SOUTH);
        }

        /** Define a largura preferida de cada coluna (em pixels). */
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
                    ? "Nenhum registro encontrado."
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
        cabecalho.setPreferredSize(new Dimension(0, 34));
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setFont(new Font(FONTE, Font.BOLD, 12));
                setBackground(new Color(244, 246, 244));
                setForeground(AZUL_TITULO);
                setHorizontalAlignment(COLUNAS_CENTRO.contains(String.valueOf(valor)) ? CENTER : LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 1, BORDA), new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });
    }
}
