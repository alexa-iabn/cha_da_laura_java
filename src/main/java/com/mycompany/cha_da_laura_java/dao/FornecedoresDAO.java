/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FornecedoresDAO {

    // LISTAR FORNECEDORES
    public List<Object[]> listarFornecedores() throws SQLException {

        List<Object[]> fornecedores = new ArrayList<>();

        String sql = """
            SELECT id, nome, cnpj, email, telefone,
                   cidade, uf, situacao
            FROM fornecedores
            ORDER BY nome
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                fornecedores.add(new Object[]{
                    resultado.getInt("id"),
                    resultado.getString("nome"),
                    resultado.getString("cnpj"),
                    resultado.getString("email"),
                    resultado.getString("telefone"),
                    resultado.getString("cidade"),
                    resultado.getString("uf"),
                    resultado.getString("situacao")
                });
            }
        }

        return fornecedores;
    }

    // CADASTRAR FORNECEDOR
    public void inserirFornecedor(
            String nome,
            String cpfCnpj,
            String email,
            String telefone,
            String cidade,
            String uf,
            String situacao
    ) throws SQLException {

        String sql = """
            INSERT INTO fornecedores
            (nome, cnpj, email, telefone, cidade, uf, situacao)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, nome);
            comando.setString(2, cpfCnpj);
            comando.setString(3, email);
            comando.setString(4, telefone);
            comando.setString(5, cidade);
            comando.setString(6, uf);
            comando.setString(7, situacao);

            comando.executeUpdate();
        }
    }

    // BUSCAR FORNECEDOR PELO CPF/CNPJ
    public Object[] buscarFornecedorPorDocumento(
            String documento
    ) throws SQLException {

        String sql = """
            SELECT id, nome, cnpj, email, telefone,
                   cidade, uf, situacao
            FROM fornecedores
            WHERE cnpj = ?
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, documento);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {

                    return new Object[]{
                        resultado.getInt("id"),
                        resultado.getString("nome"),
                        resultado.getString("cnpj"),
                        resultado.getString("email"),
                        resultado.getString("telefone"),
                        resultado.getString("cidade"),
                        resultado.getString("uf"),
                        resultado.getString("situacao")
                    };
                }
            }
        }

        return null;
    }

    // ATUALIZAR FORNECEDOR
    public void atualizarFornecedor(
            int id,
            String nome,
            String cpfCnpj,
            String email,
            String telefone,
            String cidade,
            String uf,
            String situacao
    ) throws SQLException {

        String sql = """
            UPDATE fornecedores
            SET nome = ?,
                cnpj = ?,
                email = ?,
                telefone = ?,
                cidade = ?,
                uf = ?,
                situacao = ?
            WHERE id = ?
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, nome);
            comando.setString(2, cpfCnpj);
            comando.setString(3, email);
            comando.setString(4, telefone);
            comando.setString(5, cidade);
            comando.setString(6, uf);
            comando.setString(7, situacao);
            comando.setInt(8, id);

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new SQLException("Fornecedor não encontrado.");
            }
        }
    }

    // EXCLUIR FORNECEDOR
    public void excluirFornecedor(int id) throws SQLException {

        String sql = "DELETE FROM fornecedores WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new SQLException("Fornecedor não encontrado.");
            }
        }
    }
}

