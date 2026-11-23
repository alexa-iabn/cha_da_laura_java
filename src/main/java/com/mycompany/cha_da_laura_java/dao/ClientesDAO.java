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

public class ClientesDAO {

    // Consultar todos os clientes cadastrados
    public List<Object[]> listarClientes() throws SQLException {

        List<Object[]> clientes = new ArrayList<>();

        String sql = """
            SELECT id, nome, cpf_cnpj, email, telefone,
                   cidade, uf, situacao
            FROM clientes
            ORDER BY nome
            """;

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cpf_cnpj"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getString("cidade"),
                    rs.getString("uf"),
                    rs.getString("situacao")
                });
            }
        }

        return clientes;
    }

    // Cadastrar novo cliente
    public void inserirCliente(
            String nome,
            String cpfCnpj,
            String email,
            String telefone,
            String cidade,
            String uf,
            String situacao) throws SQLException {

        String sql = """
            INSERT INTO clientes
            (nome, cpf_cnpj, email, telefone,
             cidade, uf, situacao)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, cpfCnpj);
            ps.setString(3, email);
            ps.setString(4, telefone);
            ps.setString(5, cidade);
            ps.setString(6, uf);
            ps.setString(7, situacao);

            ps.executeUpdate();
        }
    }
    

public void atualizarCliente(
        int id,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        String cidade,
        String uf,
        String situacao) throws SQLException {

    String sql = """
        UPDATE clientes
        SET nome = ?,
            cpf_cnpj = ?,
            email = ?,
            telefone = ?,
            cidade = ?,
            uf = ?,
            situacao = ?
        WHERE id = ?
        """;

    // Abre a conexão com o banco de dados
    try (Connection conexao = ConexaoBanco.conectar();
         PreparedStatement comando = conexao.prepareStatement(sql)) {

        // Define os dados atualizados do cliente
        comando.setString(1, nome);
        comando.setString(2, cpfCnpj);
        comando.setString(3, email);
        comando.setString(4, telefone);
        comando.setString(5, cidade);
        comando.setString(6, uf);
        comando.setString(7, situacao);

        // Identifica o cliente que será atualizado
        comando.setInt(8, id);

        // Executa a atualização no banco de dados
        int linhasAfetadas = comando.executeUpdate();

        if (linhasAfetadas == 0) {
            throw new SQLException("Cliente não encontrado.");
        }
    }
}

public Object[] buscarClientePorDocumento(String documento)
        throws SQLException {

    String sql = """
        SELECT id, nome, cpf_cnpj, email,
               telefone, cidade, uf, situacao
        FROM clientes
        WHERE cpf_cnpj = ?
        """;

    try (Connection conexao = ConexaoBanco.conectar();
         PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(1, documento);

        try (ResultSet resultado = comando.executeQuery()) {

            if (resultado.next()) {
                return new Object[]{
                    resultado.getInt("id"),
                    resultado.getString("nome"),
                    resultado.getString("cpf_cnpj"),
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

public void excluirCliente(int id) throws SQLException {

    String sql = "DELETE FROM clientes WHERE id = ?";

    try (Connection conexao = ConexaoBanco.conectar();
         PreparedStatement comando = conexao.prepareStatement(sql)) {

        // Identifica o cliente que será excluído
        comando.setInt(1, id);

        // Executa a exclusão no banco de dados
        int linhasAfetadas = comando.executeUpdate();

        if (linhasAfetadas == 0) {
            throw new SQLException("Cliente não encontrado.");
        }
    }
}


}

