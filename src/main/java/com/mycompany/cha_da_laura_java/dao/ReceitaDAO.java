/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import com.mycompany.cha_da_laura_java.model.Receita;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceitaDAO {

    // =====================================================
    // LISTAR TODAS AS RECEITAS
    // =====================================================
    public List<Object[]> listarReceitas() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT id_receita, id_produto, nome,
                   rendimento_unidades, rendimento_gramas,
                   custo_total, validade_dias, ativo
            FROM receitas
            ORDER BY id_receita
            """;

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_receita"),
                    rs.getInt("id_produto"),
                    rs.getString("nome"),
                    rs.getInt("rendimento_unidades"),
                    rs.getDouble("rendimento_gramas"),
                    rs.getDouble("custo_total"),
                    rs.getInt("validade_dias"),
                    rs.getBoolean("ativo") ? "Ativa" : "Inativa"
                });
            }
        }

        return lista;
    }

    // =====================================================
    // LISTAR RECEITAS ATIVAS POR PRODUTO
    // Mantém compatibilidade com o código anterior
    // =====================================================
    public List<Receita> listarPorProduto(int idProduto)
            throws SQLException {

        List<Receita> receitas = new ArrayList<>();

        String sql = """
            SELECT id_receita, id_produto, nome,
                   rendimento, unidade_rendimento,
                   descricao, ativo
            FROM receitas
            WHERE id_produto = ? AND ativo = TRUE
            ORDER BY nome
            """;

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProduto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Receita receita = new Receita();

                    receita.setIdReceita(rs.getInt("id_receita"));
                    receita.setIdProduto(rs.getInt("id_produto"));
                    receita.setNome(rs.getString("nome"));
                    receita.setRendimento(
                        rs.getBigDecimal("rendimento")
                    );
                    receita.setUnidadeRendimento(
                        rs.getString("unidade_rendimento")
                    );
                    receita.setDescricao(rs.getString("descricao"));
                    receita.setAtivo(rs.getBoolean("ativo"));

                    receitas.add(receita);
                }
            }
        }

        return receitas;
    }

    // =====================================================
    // LISTAR PRODUTOS CADASTRADOS
    // Object[] = {id, nome}
    // =====================================================
    public List<Object[]> listarProdutos() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT id_produto, nome
            FROM produtos
            WHERE ativo = 1
            ORDER BY nome
            """;

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_produto"),
                    rs.getString("nome")
                });
            }
        }

        return lista;
    }

    // =====================================================
    // LISTAR INSUMOS CADASTRADOS
    // Object[] = {id, nome, unidade_medida}
    // =====================================================
    public List<Object[]> listarInsumos() throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT id_insumo, nome, unidade_medida
            FROM insumos
            WHERE ativo = 1
            ORDER BY nome
            """;

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_insumo"),
                    rs.getString("nome"),
                    rs.getString("unidade_medida")
                });
            }
        }

        return lista;
    }

    // =====================================================
    // LISTAR INGREDIENTES DE UMA RECEITA
    // Object[] = {id_insumo, nome, quantidade, unidade}
    // =====================================================
    public List<Object[]> listarIngredientes(int idReceita)
            throws SQLException {

        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT ri.id_insumo, i.nome,
                   ri.quantidade, i.unidade_medida
            FROM receita_insumos ri
            INNER JOIN insumos i
                ON i.id_insumo = ri.id_insumo
            WHERE ri.id_receita = ?
            ORDER BY i.nome
            """;

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReceita);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("id_insumo"),
                        rs.getString("nome"),
                        rs.getBigDecimal("quantidade"),
                        rs.getString("unidade_medida")
                    });
                }
            }
        }

        return lista;
    }

    // =====================================================
    // CADASTRAR RECEITA COM INGREDIENTES
    // Cada ingrediente: {id_insumo, quantidade}
    // =====================================================
    public void inserirReceita(
            int idProduto,
            String nome,
            int unidades,
            double gramas,
            double custo,
            int validade,
            String situacao,
            List<Object[]> ingredientes
    ) throws SQLException {

        String sql = """
            INSERT INTO receitas
            (id_produto, nome, rendimento,
             unidade_rendimento, rendimento_unidades,
             rendimento_gramas, custo_total,
             validade_dias, ativo)
            VALUES (?, ?, ?, 'un', ?, ?, ?, ?, ?)
            """;

        try (Connection con = ConexaoBanco.conectar()) {

            con.setAutoCommit(false);

            try {
                int idReceita;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, idProduto);
                    ps.setString(2, nome);
                    ps.setBigDecimal(
                        3, BigDecimal.valueOf(unidades)
                    );
                    ps.setInt(4, unidades);
                    ps.setBigDecimal(
                        5, BigDecimal.valueOf(gramas)
                    );
                    ps.setBigDecimal(
                        6, BigDecimal.valueOf(custo)
                    );
                    ps.setInt(7, validade);
                    ps.setBoolean(
                        8, "Ativa".equals(situacao)
                    );

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                "Não foi possível obter o ID da receita."
                            );
                        }
                        idReceita = rs.getInt(1);
                    }
                }

                salvarIngredientes(con, idReceita, ingredientes);

                con.commit();

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // =====================================================
    // ATUALIZAR RECEITA COM INGREDIENTES
    // =====================================================
    public void atualizarReceita(
            int idReceita,
            int idProduto,
            String nome,
            int unidades,
            double gramas,
            double custo,
            int validade,
            String situacao,
            List<Object[]> ingredientes
    ) throws SQLException {

        String sql = """
            UPDATE receitas
            SET id_produto = ?,
                nome = ?,
                rendimento = ?,
                unidade_rendimento = 'un',
                rendimento_unidades = ?,
                rendimento_gramas = ?,
                custo_total = ?,
                validade_dias = ?,
                ativo = ?
            WHERE id_receita = ?
            """;

        try (Connection con = ConexaoBanco.conectar()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, idProduto);
                    ps.setString(2, nome);
                    ps.setBigDecimal(
                        3, BigDecimal.valueOf(unidades)
                    );
                    ps.setInt(4, unidades);
                    ps.setBigDecimal(
                        5, BigDecimal.valueOf(gramas)
                    );
                    ps.setBigDecimal(
                        6, BigDecimal.valueOf(custo)
                    );
                    ps.setInt(7, validade);
                    ps.setBoolean(
                        8, "Ativa".equals(situacao)
                    );
                    ps.setInt(9, idReceita);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                            "Receita não encontrada."
                        );
                    }
                }

                String excluir = """
                    DELETE FROM receita_insumos
                    WHERE id_receita = ?
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(excluir)) {
                    ps.setInt(1, idReceita);
                    ps.executeUpdate();
                }

                salvarIngredientes(con, idReceita, ingredientes);

                con.commit();

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // =====================================================
    // SALVAR INGREDIENTES
    // Utilizado dentro da mesma transação
    // =====================================================
    private void salvarIngredientes(
            Connection con,
            int idReceita,
            List<Object[]> ingredientes
    ) throws SQLException {

        if (ingredientes == null || ingredientes.isEmpty()) {
            throw new SQLException(
                "Informe pelo menos um ingrediente."
            );
        }

        String sql = """
            INSERT INTO receita_insumos
            (id_receita, id_insumo, quantidade)
            VALUES (?, ?, ?)
            """;

        java.util.Set<Integer> ids =
            new java.util.HashSet<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (Object[] ingrediente : ingredientes) {

                int idInsumo =
                    ((Number) ingrediente[0]).intValue();

                BigDecimal quantidade =
                    new BigDecimal(ingrediente[1].toString());

                if (!ids.add(idInsumo)) {
                    throw new SQLException(
                        "O mesmo insumo foi informado duas vezes."
                    );
                }

                if (quantidade.signum() <= 0) {
                    throw new SQLException(
                        "A quantidade deve ser maior que zero."
                    );
                }

                ps.setInt(1, idReceita);
                ps.setInt(2, idInsumo);
                ps.setBigDecimal(3, quantidade);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    // =====================================================
    // EXCLUIR RECEITA
    // =====================================================
    public void excluirReceita(int idReceita)
            throws SQLException {

        try (Connection con = ConexaoBanco.conectar()) {

            con.setAutoCommit(false);

            try {
                String sqlIngredientes = """
                    DELETE FROM receita_insumos
                    WHERE id_receita = ?
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(sqlIngredientes)) {

                    ps.setInt(1, idReceita);
                    ps.executeUpdate();
                }

                String sqlReceita = """
                    DELETE FROM receitas
                    WHERE id_receita = ?
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(sqlReceita)) {

                    ps.setInt(1, idReceita);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                            "Receita não encontrada."
                        );
                    }
                }

                con.commit();

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}
