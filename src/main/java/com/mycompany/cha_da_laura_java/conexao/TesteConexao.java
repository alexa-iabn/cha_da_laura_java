/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {

        String sql = "SELECT nome FROM produtos ORDER BY nome";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando =
                     conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            System.out.println("Conexão realizada com sucesso!");
            System.out.println("Produtos cadastrados:");

            int total = 0;

            while (resultado.next()) {
                System.out.println("- " + resultado.getString("nome"));
                total++;
            }

            System.out.println("Total de produtos: " + total);

        } catch (SQLException e) {
            System.err.println("Erro ao conectar ou consultar MySQL:");
            System.err.println(e.getMessage());
        }
    }
}
