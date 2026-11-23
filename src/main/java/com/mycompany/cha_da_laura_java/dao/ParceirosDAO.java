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

public class ParceirosDAO {

    // LISTAR parceiros
    public List<Object[]> listarParceiros() throws SQLException {

        List<Object[]> parceiros = new ArrayList<>();

        String sql = """
            SELECT id, nome, cnpj_cpf, email, telefone,
                   cidade, uf, situacao
            FROM parceiros
            ORDER BY nome
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                parceiros.add(new Object[]{
                    resultado.getInt("id"),
                    resultado.getString("nome"),
                    resultado.getString("cnpj_cpf"),
                    resultado.getString("email"),
                    resultado.getString("telefone"),
                    resultado.getString("cidade"),
                    resultado.getString("uf"),
                    resultado.getString("situacao")
                });
            }
        }

        return parceiros;
    }

    // CADASTRAR FORNECEDOR
    public void inserirParceiro(
            String nome,
            String cnpj_cpf,
            String email,
            String telefone,
            String cidade,
            String uf,
            String situacao
    ) throws SQLException {

        String sql = """
            INSERT INTO parceiros
            (nome, cnpj_cpf, email, telefone, cidade, uf, situacao)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, nome);
            comando.setString(2, cnpj_cpf);
            comando.setString(3, email);
            comando.setString(4, telefone);
            comando.setString(5, cidade);
            comando.setString(6, uf);
            comando.setString(7, situacao);

            comando.executeUpdate();
        }
    }

    // BUSCAR  PELO CPF/CNPJ
    public Object[] buscarParceiroPorDocumento(
            String documento
    ) throws SQLException {

        String sql = """
            SELECT id, nome, cnpj_cpf, email, telefone,
                   cidade, uf, situacao
            FROM parceiros
            WHERE cnpj_cpf = ?
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, documento);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {

                    return new Object[]{
                        resultado.getInt("id"),
                        resultado.getString("nome"),
                        resultado.getString("cnpj_cpf"),
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

    // ATUALIZAR 
    public void atualizarParceiro(
            int id,
            String nome,
            String cnpj_cpf,
            String email,
            String telefone,
            String cidade,
            String uf,
            String situacao
    ) throws SQLException {

        String sql = """
            UPDATE parceiros
            SET nome = ?,
                cnpj_cpf = ?,
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
            comando.setString(2, cnpj_cpf);
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
    public void excluirParceiro(int id) throws SQLException {

        String sql = "DELETE FROM parceiros WHERE id = ?";

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


