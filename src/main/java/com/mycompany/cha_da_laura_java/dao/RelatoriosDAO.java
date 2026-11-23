
package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelatoriosDAO {

    // Total de clientes cadastrados
    public int totalClientes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Clientes ativos ou inativos
    public int clientesPorSituacao(String situacao)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM clientes "
                   + "WHERE situacao = ?";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, situacao);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Total de compras registradas para clientes
    public int totalCompras() throws SQLException {
        String sql = "SELECT COUNT(*) FROM saidas "
                   + "WHERE destino = 'Cliente' "
                   + "AND cliente_id IS NOT NULL";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Lista de clientes com valores e última compra
    public List<Object[]> listarClientes() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT c.id, c.nome, c.email, c.cpf_cnpj, "
          + "c.telefone, c.cidade, "
          + "COALESCE(SUM(s.valor_total), 0) AS total_compras, "
          + "MAX(s.data_saida) AS ultima_compra, "
          + "c.situacao "
          + "FROM clientes c "
          + "LEFT JOIN saidas s ON s.cliente_id = c.id "
          + "AND s.destino = 'Cliente' "
          + "GROUP BY c.id, c.nome, c.email, c.cpf_cnpj, "
          + "c.telefone, c.cidade, c.situacao "
          + "ORDER BY c.id DESC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Timestamp ultima = rs.getTimestamp("ultima_compra");

                String data = ultima == null ? "-"
                    : ultima.toLocalDateTime()
                            .toLocalDate()
                            .format(java.time.format.DateTimeFormatter
                                    .ofPattern("dd/MM/yyyy"));

                lista.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("cpf_cnpj"),
                    rs.getString("telefone"),
                    rs.getString("cidade"),
                    String.format(
                        java.util.Locale.forLanguageTag("pt-BR"),
                        "R$ %,.2f",
                        rs.getDouble("total_compras")
                    ),
                    data,
                    rs.getString("situacao")
                });
            }
        }

        return lista;
    }

    // Top 5 clientes por valor total de compras
    public List<Object[]> top5Clientes() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT c.nome, COALESCE(SUM(s.valor_total), 0) AS total "
          + "FROM clientes c "
          + "INNER JOIN saidas s ON s.cliente_id = c.id "
          + "WHERE s.destino = 'Cliente' "
          + "GROUP BY c.id, c.nome "
          + "ORDER BY total DESC "
          + "LIMIT 5";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("nome"),
                    rs.getDouble("total")
                });
            }
        }

        return lista;
    }

    // Quantidade de novos clientes por mês
    public double[] clientesPorMes(int ano) throws SQLException {

        double[] meses = new double[12];

        String sql =
            "SELECT MONTH(criado_em) AS mes, COUNT(*) AS total "
          + "FROM clientes "
          + "WHERE YEAR(criado_em) = ? "
          + "GROUP BY MONTH(criado_em)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    if (mes >= 1 && mes <= 12) {
                        meses[mes - 1] = rs.getDouble("total");
                    }
                }
            }
        }

        return meses;
    }

    // Total de fornecedores cadastrados
    public int totalFornecedores() throws SQLException {

        String sql = "SELECT COUNT(*) FROM fornecedores";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Fornecedores ativos ou inativos
    public int fornecedoresPorSituacao(String situacao)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM fornecedores "
                   + "WHERE situacao = ?";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, situacao);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Lista real de fornecedores
    public List<Object[]> listarFornecedores() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT id, nome, cnpj, cidade, uf, telefone, situacao "
          + "FROM fornecedores ORDER BY id DESC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String cidade = rs.getString("cidade");
                String uf = rs.getString("uf");

                lista.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cnpj"),
                    cidade + "/" + uf,
                    rs.getString("telefone"),
                    "-", // Compras ainda não integradas
                    "-", // Última compra ainda não integrada
                    rs.getString("situacao")
                });
            }
        }

        return lista;
    }

    // Evolução mensal de cadastros de fornecedores
    public double[] fornecedoresPorMes(int ano)
            throws SQLException {

        double[] meses = new double[12];

        String sql =
            "SELECT MONTH(criado_em) AS mes, COUNT(*) AS total "
          + "FROM fornecedores "
          + "WHERE YEAR(criado_em) = ? "
          + "GROUP BY MONTH(criado_em)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int mes = rs.getInt("mes");

                    if (mes >= 1 && mes <= 12) {
                        meses[mes - 1] = rs.getDouble("total");
                    }
                }
            }
        }

        return meses;
    }

    // Total de insumos cadastrados
    public int totalInsumos() throws SQLException {

        String sql = "SELECT COUNT(*) FROM insumos";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Quantidade de insumos com estoque baixo
    public int insumosEstoqueBaixo() throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM insumos "
          + "WHERE estoque_atual < estoque_minimo "
          + "AND ativo = 1";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Lista de insumos cadastrados
    public List<Object[]> listarInsumos() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT id_insumo, nome, unidade_medida, "
          + "estoque_atual, estoque_minimo, ativo "
          + "FROM insumos ORDER BY id_insumo DESC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                double atual = rs.getDouble("estoque_atual");
                double minimo = rs.getDouble("estoque_minimo");
                boolean ativo = rs.getBoolean("ativo");

                String status;

                if (!ativo) {
                    status = "Inativo";
                } else if (atual <= 0) {
                    status = "Sem estoque";
                } else if (atual < minimo) {
                    status = "Baixo";
                } else {
                    status = "Adequado";
                }

                lista.add(new Object[]{
                    rs.getInt("id_insumo"),
                    rs.getString("nome"),
                    rs.getString("unidade_medida"),
                    atual,
                    minimo,
                    "-", // Entradas ainda não integradas
                    "-", // Consumo ainda não integrado
                    status
                });
            }
        }

        return lista;
    }

    // Insumos ativos abaixo do estoque mínimo
    public List<Object[]> listarInsumosBaixos()
            throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT nome, estoque_atual, estoque_minimo "
          + "FROM insumos "
          + "WHERE estoque_atual < estoque_minimo "
          + "AND ativo = 1 "
          + "ORDER BY estoque_atual ASC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("nome"),
                    rs.getDouble("estoque_atual"),
                    rs.getDouble("estoque_minimo")
                });
            }
        }

        return lista;
    }

    // Quantidade de movimentos de saída
    public int totalMovimentosConsumo() throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM movimentacoes_estoque "
          + "WHERE UPPER(tipo) = 'SAIDA'";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Top 5 insumos mais consumidos
    public List<Object[]> top5InsumosConsumidos()
            throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT i.nome, i.unidade_medida, "
          + "SUM(m.quantidade) AS total "
          + "FROM movimentacoes_estoque m "
          + "JOIN insumos i ON i.id_insumo = m.id_insumo "
          + "WHERE UPPER(m.tipo) = 'SAIDA' "
          + "GROUP BY i.id_insumo, i.nome, i.unidade_medida "
          + "ORDER BY total DESC LIMIT 5";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("nome"),
                    rs.getString("unidade_medida"),
                    rs.getDouble("total")
                });
            }
        }

        return lista;
    }

    // Entradas e saídas por mês
    public double[][] movimentacaoInsumosPorMes(int ano)
            throws SQLException {

        double[][] dados = new double[2][12];

        String sql =
            "SELECT MONTH(data_movimentacao) AS mes, "
          + "UPPER(tipo) AS tipo_movimento, "
          + "COUNT(*) AS total "
          + "FROM movimentacoes_estoque "
          + "WHERE YEAR(data_movimentacao) = ? "
          + "AND UPPER(tipo) IN ('ENTRADA', 'SAIDA') "
          + "GROUP BY MONTH(data_movimentacao), UPPER(tipo)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    int mes = rs.getInt("mes") - 1;
                    String tipo = rs.getString("tipo_movimento");
                    double total = rs.getDouble("total");

                    if (mes >= 0 && mes < 12) {
                        if ("ENTRADA".equals(tipo)) {
                            dados[0][mes] = total;
                        } else if ("SAIDA".equals(tipo)) {
                            dados[1][mes] = total;
                        }
                    }
                }
            }
        }

        return dados;
    }

    // Totais de entradas e consumo por insumo
    public List<Object[]> listarInsumosComMovimentos()
            throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT i.id_insumo, i.nome, i.unidade_medida, "
          + "i.estoque_atual, i.estoque_minimo, i.ativo, "
          + "COALESCE(m.entradas, 0) AS entradas, "
          + "COALESCE(m.consumo, 0) AS consumo "
          + "FROM insumos i "
          + "LEFT JOIN ("
          + " SELECT id_insumo, "
          + " SUM(CASE WHEN UPPER(tipo) = 'ENTRADA' "
          + " THEN quantidade ELSE 0 END) AS entradas, "
          + " SUM(CASE WHEN UPPER(tipo) = 'SAIDA' "
          + " THEN quantidade ELSE 0 END) AS consumo "
          + " FROM movimentacoes_estoque "
          + " GROUP BY id_insumo"
          + ") m ON m.id_insumo = i.id_insumo "
          + "ORDER BY i.id_insumo DESC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                double atual = rs.getDouble("estoque_atual");
                double minimo = rs.getDouble("estoque_minimo");
                boolean ativo = rs.getBoolean("ativo");

                String status;

                if (!ativo) {
                    status = "Inativo";
                } else if (atual <= 0) {
                    status = "Sem estoque";
                } else if (atual < minimo) {
                    status = "Baixo";
                } else {
                    status = "Adequado";
                }

                lista.add(new Object[]{
                    rs.getInt("id_insumo"),
                    rs.getString("nome"),
                    rs.getString("unidade_medida"),
                    atual,
                    minimo,
                    rs.getDouble("entradas"),
                    rs.getDouble("consumo"),
                    status
                });
            }
        }

        return lista;
    }

    // Indicadores gerais de produção
    public int[] indicadoresProducao() throws SQLException {

        String sql =
            "SELECT COUNT(*) AS total_producoes, "
          + "COALESCE(SUM(quantidade), 0) AS total_unidades, "
          + "COUNT(DISTINCT id_produto) AS produtos_diferentes "
          + "FROM producoes";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new int[]{
                    rs.getInt("total_producoes"),
                    rs.getInt("total_unidades"),
                    rs.getInt("produtos_diferentes")
                };
            }
        }

        return new int[]{0, 0, 0};
    }

    // Quantidade de registros de insumos utilizados
    public int totalRegistrosInsumosProducao()
            throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM producao_insumos";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Produção agrupada por produto
    public List<Object[]> producaoPorProduto()
            throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT p.nome, SUM(pr.quantidade) AS total "
          + "FROM producoes pr "
          + "JOIN produtos p ON p.id_produto = pr.id_produto "
          + "GROUP BY p.id_produto, p.nome "
          + "ORDER BY total DESC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("nome"),
                    rs.getDouble("total")
                });
            }
        }

        return lista;
    }

    // Evolução mensal da quantidade produzida
    public double[] producaoPorMes(int ano)
            throws SQLException {

        double[] meses = new double[12];

        String sql =
            "SELECT MONTH(data_producao) AS mes, "
          + "SUM(quantidade) AS total "
          + "FROM producoes "
          + "WHERE YEAR(data_producao) = ? "
          + "GROUP BY MONTH(data_producao)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int mes = rs.getInt("mes");

                    if (mes >= 1 && mes <= 12) {
                        meses[mes - 1] = rs.getDouble("total");
                    }
                }
            }
        }

        return meses;
    }

    // Lista de produções e principais insumos utilizados
    public List<Object[]> listarProducoes()
            throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql =
            "SELECT pr.id_producao, pr.data_producao, "
          + "p.nome AS produto, pr.quantidade, "
          + "COALESCE(ins.insumos, '-') AS insumos, "
          + "pr.usuario, pr.status "
          + "FROM producoes pr "
          + "JOIN produtos p ON p.id_produto = pr.id_produto "
          + "LEFT JOIN ("
          + " SELECT pi.id_producao, "
          + " GROUP_CONCAT(DISTINCT i.nome "
          + " ORDER BY i.nome SEPARATOR ', ') AS insumos "
          + " FROM producao_insumos pi "
          + " JOIN insumos i ON i.id_insumo = pi.id_insumo "
          + " GROUP BY pi.id_producao"
          + ") ins ON ins.id_producao = pr.id_producao "
          + "ORDER BY pr.data_producao DESC, pr.id_producao DESC";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            java.time.format.DateTimeFormatter formato =
                java.time.format.DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
                );

            while (rs.next()) {

                java.sql.Date data =
                    rs.getDate("data_producao");

                String dataFormatada = data == null
                    ? "-"
                    : data.toLocalDate().format(formato);

                lista.add(new Object[]{
                    rs.getInt("id_producao"),
                    dataFormatada,
                    rs.getString("produto"),
                    rs.getInt("quantidade"),
                    rs.getString("insumos"),
                    rs.getString("usuario") == null
                        ? "-" : rs.getString("usuario"),
                    rs.getString("status")
                });
            }
        }

        return lista;
    }


    // Movimentações reais (contagens de registros, sem somar unidades incompatíveis)
    public int[] indicadoresMovimentacao() throws SQLException {
        int[] valores = new int[3];
        String sql = "SELECT UPPER(tipo) tipo, COUNT(*) total FROM movimentacoes_estoque GROUP BY UPPER(tipo)";
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                int qtd = rs.getInt("total");
                valores[2] += qtd;
                if ("ENTRADA".equals(tipo)) valores[0] += qtd;
                if ("SAIDA".equals(tipo) || "SAÍDA".equals(tipo)) valores[1] += qtd;
            }
        }
        return valores;
    }

    public double[][] movimentacoesPorMes(int ano) throws SQLException {
        double[][] dados = new double[2][12];
        String sql = "SELECT MONTH(data_movimentacao) mes, UPPER(tipo) tipo, COUNT(*) total "
            + "FROM movimentacoes_estoque WHERE YEAR(data_movimentacao)=? "
            + "GROUP BY MONTH(data_movimentacao), UPPER(tipo)";
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ano);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("mes") - 1;
                    String t = rs.getString("tipo");
                    if (m < 0 || m >= 12) continue;
                    if ("ENTRADA".equals(t)) dados[0][m] = rs.getDouble("total");
                    if ("SAIDA".equals(t) || "SAÍDA".equals(t)) dados[1][m] = rs.getDouble("total");
                }
            }
        }
        return dados;
    }

    public List<Object[]> topProdutosSaidas() throws SQLException {
        List<Object[]> dados = new ArrayList<>();
        String sql = "SELECT p.nome, SUM(m.quantidade) total FROM movimentacoes_estoque m "
            + "JOIN produtos p ON p.id_produto=m.id_produto "
            + "WHERE UPPER(m.tipo) IN ('SAIDA','SAÍDA') "
            + "GROUP BY p.id_produto, p.nome ORDER BY total DESC LIMIT 5";
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) dados.add(new Object[]{rs.getString(1), rs.getDouble(2)});
        }
        return dados;
    }

    public List<Object[]> motivosSaidas() throws SQLException {
        List<Object[]> dados = new ArrayList<>();
        String sql = "SELECT COALESCE(NULLIF(TRIM(motivo),''),'Não informado') motivo, "
            + "COUNT(*) total FROM movimentacoes_estoque "
            + "WHERE UPPER(tipo) IN ('SAIDA','SAÍDA') GROUP BY motivo ORDER BY total DESC LIMIT 6";
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) dados.add(new Object[]{rs.getString(1), rs.getDouble(2)});
        }
        return dados;
    }

    public List<Object[]> listarMovimentacoes() throws SQLException {
        List<Object[]> dados = new ArrayList<>();
        String sql = "SELECT m.id_movimentacao, m.data_movimentacao, m.tipo, "
            + "COALESCE(p.nome,i.nome,'-') item, m.quantidade, m.motivo, m.usuario, "
            + "m.observacoes FROM movimentacoes_estoque m "
            + "LEFT JOIN produtos p ON p.id_produto=m.id_produto "
            + "LEFT JOIN insumos i ON i.id_insumo=m.id_insumo "
            + "ORDER BY m.data_movimentacao DESC, m.id_movimentacao DESC";
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp t = rs.getTimestamp(2);
                dados.add(new Object[]{rs.getInt(1), t == null ? "-" : t.toLocalDateTime().toLocalDate().format(f),
                    rs.getString(3), rs.getString(4), rs.getBigDecimal(5),
                    rs.getString(6), rs.getString(7), rs.getString(8)});
            }
        }
        return dados;
    }

}
