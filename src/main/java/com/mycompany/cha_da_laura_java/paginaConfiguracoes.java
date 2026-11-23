package com.mycompany.cha_da_laura_java;

import com.mycompany.cha_da_laura_java.dao.ConfiguracoesDAO;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class paginaConfiguracoes extends JPanel {

    // ===== Cores e fonte do layout =====
    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color AZUL_INFO = new Color(238, 243, 252);
    private static final Color AZUL_INFO_BORDA = new Color(212, 224, 246);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color BRANCO = Color.WHITE;
    private static final String FONTE = "Segoe UI";

    // ===== Abas =====
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel painelAbas = new JPanel(cardLayout);
    private final JButton[] botoesAbas = new JButton[3];
    private final String[] nomesCards = {"usuario", "seguranca", "alertas"};

    // ===== Aba "Meu usuário" =====
    private JComboBox<ConfiguracoesDAO.Usuario> cbUsuario;
    private ConfiguracoesDAO.Usuario usuarioSelecionado;
    private final ConfiguracoesDAO configuracoesDAO = new ConfiguracoesDAO();
    private final Preferences preferencias = Preferences.userNodeForPackage(paginaConfiguracoes.class);
    private JTextField txtNome;
    private JTextField txtEmail;
    private JTextField txtLogin;
    private JComboBox<String> cbPerfil;
    private JLabel lblFoto;
    private File fotoSelecionada;

    // ===== Aba "Segurança" =====
    private JPasswordField txtSenhaAtual;
    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmarSenha;
    private JComboBox<String> cbInatividade;

    // ===== Aba "Alertas de estoque" =====
    private BotaoSwitch swAlertas;
    private JCheckBox chkNotificacao;
    private JCheckBox chkEmail;
    private JComboBox<String> cbFrequencia;

    public paginaConfiguracoes() {
        setLayout(new BorderLayout());
        setBackground(BRANCO);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(criarCabecalho(), BorderLayout.NORTH);

        painelAbas.setOpaque(false);
        painelAbas.add(criarAbaUsuario(), nomesCards[0]);
        painelAbas.add(criarAbaSeguranca(), nomesCards[1]);
        painelAbas.add(criarAbaAlertas(), nomesCards[2]);

        JScrollPane scroll = new JScrollPane(painelAbas);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BRANCO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        selecionarAba(0);
        carregarDados();
    }

    // =====================================================================
    // CABEÇALHO + BOTÕES DAS ABAS
    // =====================================================================
    private JPanel criarCabecalho() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel titulo = new JLabel("Configurações");
        titulo.setFont(new Font(FONTE, Font.BOLD, 32));
        titulo.setForeground(AZUL_TITULO);

        JLabel subtitulo = new JLabel("Gerencie seu perfil, segurança e alertas do sistema.");
        subtitulo.setFont(new Font(FONTE, Font.PLAIN, 15));
        subtitulo.setForeground(AZUL_TEXTO);

        JPanel abas = new JPanel(new GridLayout(1, 3, 10, 0));
        abas.setOpaque(false);
        String[] nomes = {"Meu usuário", "Segurança", "Alertas de estoque"};
        for (int i = 0; i < nomes.length; i++) {
            final int indice = i;
            JButton b = new JButton(nomes[i]);
            b.setFont(new Font(FONTE, Font.PLAIN, 15));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(200, 50));
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
        cardLayout.show(painelAbas, nomesCards[indice]);
        for (int i = 0; i < botoesAbas.length; i++) {
            boolean ativo = (i == indice);
            pintarBotao(botoesAbas[i],
                    ativo ? VERDE : BRANCO,
                    ativo ? BRANCO : VERDE,
                    ativo ? VERDE : BORDA);
        }
    }

    // =====================================================================
    // ABA 1: MEU USUÁRIO
    // =====================================================================
    private JPanel criarAbaUsuario() {
        JPanel card = criarCard();

        cbUsuario = new JComboBox<>();
        cbUsuario.setPreferredSize(new Dimension(250,40));
        cbUsuario.addActionListener(e -> preencherUsuario());
        txtNome = criarTexto();
        txtEmail = criarTexto();
        txtLogin = criarTexto();
        cbPerfil = new JComboBox<>(new String[]{"Administrador", "Operador"});
        estilizarCombo(cbPerfil);
        cbPerfil.setEnabled(false); // o perfil não é alterado pelo próprio usuário

        // --- Formulário (lado esquerdo) ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints f = new GridBagConstraints();
        f.fill = GridBagConstraints.HORIZONTAL;
        f.insets = new Insets(0, 0, 16, 16);

        f.gridx = 0; f.gridy = 0; f.gridwidth = 2; f.weightx = 1;
        form.add(campo("Selecionar usuário", cbUsuario), f);
        f.gridy = 1;
        form.add(campo("Nome completo *", txtNome), f);

        f.gridx = 0; f.gridy = 2; f.gridwidth = 1; f.weightx = 0.6;
        form.add(campo("E-mail *", txtEmail), f);

        f.gridx = 1; f.gridy = 2; f.weightx = 0.4;
        txtLogin.setEditable(false);
        form.add(campo("ID do usuário (sem login)", txtLogin), f);

        f.gridx = 0; f.gridy = 3; f.gridwidth = 2; f.weightx = 1;
        form.add(campo("Perfil", cbPerfil), f);

        // --- Foto (lado direito) ---
        lblFoto = new JLabel("Sem foto", SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(140, 140));
        lblFoto.setMaximumSize(new Dimension(140, 140));
        lblFoto.setOpaque(true);
        lblFoto.setBackground(VERDE_CLARO);
        lblFoto.setForeground(VERDE);
        lblFoto.setFont(new Font(FONTE, Font.PLAIN, 14));
        lblFoto.setBorder(new LineBorder(BORDA, 1, true));

        JButton btFoto = new JButton("Alterar foto");
        btFoto.setFont(new Font(FONTE, Font.PLAIN, 15));
        btFoto.setFocusPainted(false);
        btFoto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pintarBotao(btFoto, VERDE_CLARO, VERDE, BORDA);
        btFoto.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(10, 26, 10, 26)));
        btFoto.addActionListener(e -> escolherFoto());

        JLabel formatos = new JLabel("Formatos aceitos: JPG, PNG (máx. 2MB)");
        formatos.setFont(new Font(FONTE, Font.PLAIN, 12));
        formatos.setForeground(AZUL_TEXTO);

        JPanel ladoFoto = new JPanel();
        ladoFoto.setLayout(new BoxLayout(ladoFoto, BoxLayout.Y_AXIS));
        ladoFoto.setOpaque(false);
        ladoFoto.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 0, 0, BORDA), new EmptyBorder(0, 40, 0, 20)));
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        formatos.setAlignmentX(Component.CENTER_ALIGNMENT);
        ladoFoto.add(lblFoto);
        ladoFoto.add(Box.createVerticalStrut(16));
        ladoFoto.add(btFoto);
        ladoFoto.add(Box.createVerticalStrut(8));
        ladoFoto.add(formatos);

        JButton btSalvar = botaoVerde("Salvar alterações");
        btSalvar.addActionListener(e -> salvarUsuario());

        // --- Montagem do card ---
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        card.add(criarTituloCard("Meu usuário",
                "Visualize e altere seus dados de acesso ao sistema."), g);

        g.gridy = 1; g.gridwidth = 1; g.weightx = 1;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(20, 0, 0, 0);
        card.add(form, g);

        g.gridx = 1; g.weightx = 0;
        g.fill = GridBagConstraints.VERTICAL;
        card.add(ladoFoto, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(10, 0, 0, 0);
        card.add(btSalvar, g);

        return empilhar(card);
    }

    private void escolherFoto() {
        JFileChooser escolha = new JFileChooser();
        escolha.setFileFilter(new FileNameExtensionFilter("Imagens (JPG, PNG)", "jpg", "jpeg", "png"));
        if (escolha.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = escolha.getSelectedFile();
        if (arquivo.length() > 2 * 1024 * 1024) {
            aviso("A imagem deve ter no máximo 2MB.");
            return;
        }
        Image img = new ImageIcon(arquivo.getAbsolutePath()).getImage()
                .getScaledInstance(140, 140, Image.SCALE_SMOOTH);
        lblFoto.setText("");
        lblFoto.setIcon(new ImageIcon(img));
        fotoSelecionada = arquivo;
    }

    private void preencherUsuario() {
        usuarioSelecionado=(ConfiguracoesDAO.Usuario)cbUsuario.getSelectedItem();
        if(usuarioSelecionado==null) return;
        txtNome.setText(usuarioSelecionado.nome);
        txtEmail.setText(usuarioSelecionado.email);
        txtLogin.setText(String.valueOf(usuarioSelecionado.id));
        cbPerfil.setSelectedItem(usuarioSelecionado.perfil);
    }

    private void salvarUsuario() {
        if(usuarioSelecionado==null) { aviso("Selecione um usuário."); return; }
        String nome=txtNome.getText().trim(), email=txtEmail.getText().trim();
        if(nome.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            aviso("Informe nome e e-mail válido."); return;
        }
        int id=usuarioSelecionado.id;
        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                configuracoesDAO.atualizarUsuario(id,nome,email); return null;
            }
            @Override protected void done() {
                try { get(); sucesso("Dados salvos no MySQL!"); carregarDados(); }
                catch(Exception e) { aviso("Erro ao salvar: " + e.getCause()); }
            }
        }.execute();
    }

    // =====================================================================
    // ABA 2: SEGURANÇA
    // =====================================================================
    private JPanel criarAbaSeguranca() {
        // ---------- Card "Alterar senha" ----------
        JPanel cardSenha = criarCard();

        txtSenhaAtual = new JPasswordField();
        txtNovaSenha = new JPasswordField();
        txtConfirmarSenha = new JPasswordField();

        JPanel campos = new JPanel(new GridLayout(3, 1, 0, 14));
        campos.setOpaque(false);
        campos.add(campo("Senha atual *", campoSenha(txtSenhaAtual)));
        campos.add(campo("Nova senha *", campoSenha(txtNovaSenha)));
        campos.add(campo("Confirmar nova senha *", campoSenha(txtConfirmarSenha)));

        JButton btAlterar = botaoVerde("Alterar senha");
        btAlterar.addActionListener(e -> alterarSenha());

        JPanel ladoDireito = new JPanel(new BorderLayout());
        ladoDireito.setOpaque(false);
        ladoDireito.add(caixaInfo("Requisitos da senha", true, 300,
                "Pelo menos 8 caracteres",
                "Incluir letras e números",
                "Recomendamos o uso de uma letra maiúscula",
                "Evite senhas fáceis de adivinhar"), BorderLayout.NORTH);
        ladoDireito.add(alinharDireita(btAlterar), BorderLayout.SOUTH);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        cardSenha.add(criarTituloCard("Alterar senha",
                "Defina uma nova senha para acessar o sistema."), g);

        g.gridy = 1; g.gridwidth = 1; g.weightx = 0.55;
        g.insets = new Insets(20, 0, 0, 30);
        cardSenha.add(campos, g);

        g.gridx = 1; g.weightx = 0.45;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(20, 0, 0, 0);
        cardSenha.add(ladoDireito, g);

        // ---------- Card "Tempo de inatividade" ----------
        JPanel cardInatividade = criarCard();

        cbInatividade = new JComboBox<>(new String[]{
            "5 minutos", "15 minutos", "30 minutos", "1 hora", "Nunca"});
        cbInatividade.setSelectedItem("30 minutos");
        estilizarCombo(cbInatividade);

        JButton btSalvarInatividade = botaoVerde("Salvar alteração");
        btSalvarInatividade.addActionListener(e -> salvarInatividade());

        g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        cardInatividade.add(criarTituloCard("Tempo de inatividade",
                "Defina o tempo sem atividade para o sistema encerrar sua sessão automaticamente."), g);

        g.gridy = 1; g.gridwidth = 1; g.weightx = 0.45;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(20, 0, 0, 30);
        cardInatividade.add(campo("Tempo de inatividade para logout", cbInatividade), g);

        g.gridx = 1; g.weightx = 0.55;
        g.insets = new Insets(20, 0, 0, 0);
        cardInatividade.add(caixaInfo(null, false, 360,
                "Após o período sem atividade, sua sessão será encerrada automaticamente por segurança."), g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(16, 0, 0, 0);
        cardInatividade.add(btSalvarInatividade, g);

        return empilhar(cardSenha, cardInatividade);
    }

    private void alterarSenha() {
        aviso("Alteração de senha indisponível: o sistema ainda não possui login nem autenticação de senha. Nenhuma senha foi modificada.");
    }

    private void salvarInatividade() {
        preferencias.put("inatividade", String.valueOf(cbInatividade.getSelectedItem()));
        sucesso("Preferência salva neste computador. Logout automático exige login.");
    }

    // =====================================================================
    // ABA 3: ALERTAS DE ESTOQUE
    // =====================================================================
    private JPanel criarAbaAlertas() {
        // ---------- Card "Alertas de estoque" ----------
        JPanel cardAlertas = criarCard();

        swAlertas = new BotaoSwitch();
        swAlertas.setSelected(true);
        chkNotificacao = new JCheckBox();
        chkNotificacao.setOpaque(false);
        chkNotificacao.setSelected(true);
        chkEmail = new JCheckBox();
        chkEmail.setOpaque(false);
        chkEmail.setSelected(true);

        JLabel lblComoReceber = new JLabel("Como receber os alertas");
        lblComoReceber.setFont(new Font(FONTE, Font.BOLD, 16));
        lblComoReceber.setForeground(AZUL_TITULO);

        JPanel opcoes = new JPanel();
        opcoes.setLayout(new BoxLayout(opcoes, BoxLayout.Y_AXIS));
        opcoes.setOpaque(false);
        JPanel opSwitch = opcao(swAlertas, "Ativar alertas de estoque",
                "Quando ativado, você receberá notificações dos itens com estoque baixo.");
        JPanel opNotif = opcao(chkNotificacao, "Notificação no sistema",
                "Preferência para futura integração de notificações.");
        JPanel opEmail = opcao(chkEmail, "E-mail",
                "Preferência para futura integração de e-mail.");
        opSwitch.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblComoReceber.setAlignmentX(Component.LEFT_ALIGNMENT);
        opNotif.setAlignmentX(Component.LEFT_ALIGNMENT);
        opEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        opcoes.add(opSwitch);
        opcoes.add(Box.createVerticalStrut(24));
        opcoes.add(lblComoReceber);
        opcoes.add(Box.createVerticalStrut(12));
        opcoes.add(opNotif);
        opcoes.add(Box.createVerticalStrut(12));
        opcoes.add(opEmail);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        cardAlertas.add(criarTituloCard("Alertas de estoque",
                "Defina como e quando o sistema deve te avisar sobre itens com estoque baixo."), g);

        g.gridy = 1; g.gridwidth = 1; g.weightx = 0.55;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(20, 0, 0, 30);
        cardAlertas.add(opcoes, g);

        g.gridx = 1; g.weightx = 0.45;
        g.insets = new Insets(20, 0, 0, 0);
        cardAlertas.add(caixaInfo("Como funciona?", true, 300,
                "O sistema verifica os itens que estão abaixo do estoque mínimo definido no cadastro.",
                "Você será avisado quando algum insumo ou produto atingir o limite.",
                "O estoque mínimo continua sendo configurado individualmente em cada item."), g);

        // ---------- Card "Frequência de verificação" ----------
        JPanel cardFrequencia = criarCard();

        cbFrequencia = new JComboBox<>(new String[]{
            "30 minutos", "1 hora", "6 horas", "12 horas", "1 dia"});
        cbFrequencia.setSelectedItem("1 hora");
        estilizarCombo(cbFrequencia);

        JPanel campoFrequencia = campo("Verificar estoque a cada", cbFrequencia);
        campoFrequencia.setPreferredSize(new Dimension(380, 70));

        g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        cardFrequencia.add(criarTituloCard("Frequência de verificação",
                "Defina com qual frequência o sistema verifica o estoque."), g);

        g.gridy = 1;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(20, 0, 0, 0);
        cardFrequencia.add(campoFrequencia, g);

        // Ligar/desligar o switch habilita ou desabilita as outras opções
        swAlertas.addActionListener(e -> atualizarAlertas());
        atualizarAlertas();

        JButton btSalvar = botaoVerde("Salvar alterações");
        btSalvar.addActionListener(e -> salvarAlertas());

        return empilhar(cardAlertas, cardFrequencia, alinharDireita(btSalvar));
    }

    private void atualizarAlertas() {
        boolean ligado = swAlertas.isSelected();
        chkNotificacao.setEnabled(ligado);
        chkEmail.setEnabled(ligado);
        cbFrequencia.setEnabled(ligado);
    }

    private void salvarAlertas() {
        if (swAlertas.isSelected() && !chkNotificacao.isSelected() && !chkEmail.isSelected()) {
            aviso("Selecione pelo menos uma forma de receber os alertas.");
            return;
        }
        preferencias.putBoolean("alertas", swAlertas.isSelected());
        preferencias.putBoolean("notificacao", chkNotificacao.isSelected());
        preferencias.putBoolean("email", chkEmail.isSelected());
        preferencias.put("frequencia", String.valueOf(cbFrequencia.getSelectedItem()));
        sucesso("Preferências salvas neste computador. Envio de e-mails e verificações automáticas ainda não estão implementados.");
    }

    // =====================================================================
    // CARREGAR DADOS DO USUÁRIO LOGADO
    // =====================================================================
    private void carregarDados() {
        cbInatividade.setSelectedItem(preferencias.get("inatividade","30 minutos"));
        swAlertas.setSelected(preferencias.getBoolean("alertas",true));
        chkNotificacao.setSelected(preferencias.getBoolean("notificacao",true));
        chkEmail.setSelected(preferencias.getBoolean("email",false));
        cbFrequencia.setSelectedItem(preferencias.get("frequencia","1 hora"));
        atualizarAlertas();
        int anterior=usuarioSelecionado==null ? -1 : usuarioSelecionado.id;
        new SwingWorker<List<ConfiguracoesDAO.Usuario>,Void>() {
            @Override protected List<ConfiguracoesDAO.Usuario> doInBackground() throws Exception {
                return configuracoesDAO.listarUsuarios();
            }
            @Override protected void done() {
                try {
                    List<ConfiguracoesDAO.Usuario> usuarios=get();
                    cbUsuario.removeAllItems();
                    for(ConfiguracoesDAO.Usuario u:usuarios) cbUsuario.addItem(u);
                    for(int i=0;i<cbUsuario.getItemCount();i++) {
                        if(cbUsuario.getItemAt(i).id==anterior) {cbUsuario.setSelectedIndex(i);break;}
                    }
                    preencherUsuario();
                } catch(Exception e) { aviso("Erro ao carregar usuários: " + e.getCause()); }
            }
        }.execute();
    }

    // =====================================================================
    // MÉTODOS AUXILIARES (para não repetir código)
    // =====================================================================
    private JPanel criarCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(22, 28, 22, 28)));
        return card;
    }

    private JPanel criarTituloCard(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.BOLD, 22));
        t.setForeground(AZUL_TITULO);

        JLabel s = new JLabel(subtitulo);
        s.setFont(new Font(FONTE, Font.PLAIN, 14));
        s.setForeground(AZUL_TEXTO);

        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(s);
        return p;
    }

    /** Coloca vários cards um embaixo do outro, sem esticar na altura. */
    private JPanel empilhar(JComponent... itens) {
        JPanel coluna = new JPanel();
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        coluna.setOpaque(false);
        for (JComponent item : itens) {
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            coluna.add(item);
            coluna.add(Box.createVerticalStrut(16));
        }
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(coluna, BorderLayout.NORTH);
        return envoltorio;
    }

    /** Rótulo em cima + componente embaixo. */
    private JPanel campo(String rotulo, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(rotulo);
        l.setFont(new Font(FONTE, Font.PLAIN, 14));
        l.setForeground(AZUL_TITULO);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private JTextField criarTexto() {
        JTextField t = new JTextField();
        t.setFont(new Font(FONTE, Font.PLAIN, 14));
        t.setPreferredSize(new Dimension(200, 40));
        t.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(0, 12, 0, 12)));
        return t;
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font(FONTE, Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(200, 40));
        combo.setBackground(BRANCO);
    }

    /** Campo de senha com botão "Mostrar/Ocultar" (o "olhinho"). */
    private JPanel campoSenha(JPasswordField campo) {
        final char ecoOriginal = campo.getEchoChar();
        campo.setFont(new Font(FONTE, Font.PLAIN, 14));
        campo.setBorder(new EmptyBorder(0, 12, 0, 12));

        JToggleButton olho = new JToggleButton("Mostrar");
        olho.setFont(new Font(FONTE, Font.PLAIN, 12));
        olho.setForeground(VERDE);
        olho.setContentAreaFilled(false);
        olho.setBorderPainted(false);
        olho.setFocusPainted(false);
        olho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        olho.addActionListener(e -> {
            if (olho.isSelected()) {
                campo.setEchoChar((char) 0); // mostra o texto
                olho.setText("Ocultar");
            } else {
                campo.setEchoChar(ecoOriginal);
                olho.setText("Mostrar");
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BRANCO);
        p.setPreferredSize(new Dimension(200, 40));
        p.setBorder(new LineBorder(BORDA, 1, true));
        p.add(campo, BorderLayout.CENTER);
        p.add(olho, BorderLayout.EAST);
        return p;
    }

    /** Caixa azul de informação. largura = largura do texto em pixels (para quebrar linha). */
    private JPanel caixaInfo(String titulo, boolean marcadores, int largura, String... itens) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AZUL_INFO);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AZUL_INFO_BORDA, 1, true), new EmptyBorder(16, 18, 16, 18)));

        if (titulo != null) {
            JLabel t = new JLabel(titulo);
            t.setFont(new Font(FONTE, Font.BOLD, 15));
            t.setForeground(AZUL_TITULO);
            p.add(t);
            p.add(Box.createVerticalStrut(8));
        }
        for (String item : itens) {
            String texto = marcadores ? "• " + item : item;
            JLabel l = new JLabel("<html><div style='width:" + largura + "px'>" + texto + "</div></html>");
            l.setFont(new Font(FONTE, Font.PLAIN, 13));
            l.setForeground(AZUL_TITULO);
            p.add(l);
            p.add(Box.createVerticalStrut(4));
        }
        return p;
    }

    /** Linha com um controle (switch/checkbox) e dois textos ao lado. */
    private JPanel opcao(JComponent controle, String titulo, String descricao) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);

        JPanel caixaControle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        caixaControle.setOpaque(false);
        caixaControle.add(controle);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.PLAIN, 15));
        t.setForeground(AZUL_TITULO);
        JLabel d = new JLabel(descricao);
        d.setFont(new Font(FONTE, Font.PLAIN, 12));
        d.setForeground(AZUL_TEXTO);
        textos.add(t);
        textos.add(d);

        p.add(caixaControle, BorderLayout.WEST);
        p.add(textos, BorderLayout.CENTER);
        return p;
    }

    private JPanel alinharDireita(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JButton botaoVerde(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font(FONTE, Font.PLAIN, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pintarBotao(b, VERDE, BRANCO, VERDE);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE, 1, true), new EmptyBorder(12, 28, 12, 28)));
        return b;
    }

    /** No Windows o JButton ignora setBackground; desligar o contentArea resolve. */
    private void pintarBotao(JButton b, Color fundo, Color texto, Color borda) {
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(fundo);
        b.setForeground(texto);
        b.setBorder(new LineBorder(borda, 1, true));
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void sucesso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    // BOTÃO TIPO "SWITCH" (liga/desliga) desenhado à mão
    // =====================================================================
    private static class BotaoSwitch extends JToggleButton {

        BotaoSwitch() {
            setPreferredSize(new Dimension(52, 28));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(isSelected() ? VERDE : new Color(200, 200, 200));
            g2.fillRoundRect(0, 0, w, h, h, h);
            int d = h - 6;
            int x = isSelected() ? w - d - 3 : 3;
            g2.setColor(Color.WHITE);
            g2.fillOval(x, 3, d, d);
            g2.dispose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(1100, 756));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>                        


    // Variables declaration - do not modify                     
    // End of variables declaration                   
}

