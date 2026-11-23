/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    private static final String URL =
        System.getenv("CHAS_DB_URL");

    private static final String USUARIO =
        System.getenv("CHAS_DB_USER");

    private static final String SENHA =
        System.getenv("CHAS_DB_PASSWORD");

    public static Connection conectar() throws SQLException {

        if (URL == null || USUARIO == null || SENHA == null) {
            throw new SQLException(
                "Configuração do banco de dados não encontrada."
            );
        }

        return DriverManager.getConnection(
            URL, USUARIO, SENHA
        );
    }
}

