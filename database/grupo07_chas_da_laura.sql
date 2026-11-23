-- phpMyAdmin SQL Dump
-- version 4.9.7
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 23-11-2026 a las 19:33:59
-- Versión del servidor: 8.0.25
-- Versión de PHP: 7.3.29-1~deb10u1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `grupo07_chas_da_laura`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id` int NOT NULL,
  `nome` varchar(60) NOT NULL,
  `tipo` enum('INSUMO','PRODUTO','RECEITA') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id` int NOT NULL,
  `nome` varchar(120) NOT NULL,
  `cpf_cnpj` varchar(18) NOT NULL,
  `email` varchar(120) NOT NULL,
  `telefone` varchar(20) NOT NULL,
  `rua` varchar(120) DEFAULT NULL,
  `numero` varchar(10) DEFAULT NULL,
  `complemento` varchar(60) DEFAULT NULL,
  `bairro` varchar(80) DEFAULT NULL,
  `cidade` varchar(80) NOT NULL,
  `uf` char(2) NOT NULL,
  `cep` varchar(9) DEFAULT NULL,
  `situacao` enum('Ativo','Inativo') NOT NULL DEFAULT 'Ativo',
  `observacoes` text,
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id`, `nome`, `cpf_cnpj`, `email`, `telefone`, `rua`, `numero`, `complemento`, `bairro`, `cidade`, `uf`, `cep`, `situacao`, `observacoes`, `criado_em`) VALUES
(2, 'testinho', '123.456.895-41', 'teste@gmaill.com', '(55) 45632-5488', NULL, NULL, NULL, NULL, 'foz', 'PR', NULL, 'Ativo', NULL, '2026-11-22 21:25:48'),
(3, 'teste2', '154.789.645-23', 'testinho@gmail.com', '(55) 4562-1585', NULL, NULL, NULL, NULL, 'pipi', 'PR', NULL, 'Ativo', NULL, '2026-11-22 21:27:53'),
(4, 'apala', '45.698.745/2156-58', 'apala@gmail.com', '(35) 6452-1548', NULL, NULL, NULL, NULL, 'foz', 'UF', NULL, 'Ativo', NULL, '2026-11-22 21:48:09');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `fornecedores`
--

CREATE TABLE `fornecedores` (
  `id` int NOT NULL,
  `nome` varchar(120) NOT NULL,
  `cnpj` varchar(18) NOT NULL,
  `email` varchar(120) NOT NULL,
  `telefone` varchar(20) NOT NULL,
  `rua` varchar(120) DEFAULT NULL,
  `numero` varchar(10) DEFAULT NULL,
  `complemento` varchar(60) DEFAULT NULL,
  `bairro` varchar(80) DEFAULT NULL,
  `cidade` varchar(80) NOT NULL,
  `uf` char(2) NOT NULL,
  `cep` varchar(9) DEFAULT NULL,
  `situacao` enum('Ativo','Inativo') NOT NULL DEFAULT 'Ativo',
  `observacoes` text,
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `insumos`
--

CREATE TABLE `insumos` (
  `id_insumo` int NOT NULL,
  `nome` varchar(150) NOT NULL,
  `categoria` varchar(100) NOT NULL DEFAULT 'Sem categoria',
  `unidade_medida` varchar(20) NOT NULL,
  `estoque_atual` decimal(12,2) NOT NULL DEFAULT '0.00',
  `estoque_minimo` decimal(12,2) NOT NULL DEFAULT '0.00',
  `ativo` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `insumos`
--

INSERT INTO `insumos` (`id_insumo`, `nome`, `categoria`, `unidade_medida`, `estoque_atual`, `estoque_minimo`, `ativo`) VALUES
(1, 'Camomila', 'Sem categoria', 'g', '5000.00', '500.00', 1),
(2, 'Erva-doce', 'Sem categoria', 'g', '4000.00', '400.00', 1),
(3, 'Capim-limão', 'Sem categoria', 'g', '4000.00', '400.00', 1),
(4, 'Hibisco', 'Sem categoria', 'g', '4980.00', '500.00', 1),
(5, 'Morango desidratado', 'Sem categoria', 'g', '3000.00', '300.00', 1),
(6, 'Pétalas de rosa', 'Sem categoria', 'g', '2980.00', '300.00', 1),
(7, 'Melissa', 'Sem categoria', 'g', '4000.00', '400.00', 1),
(8, 'Hortelã', 'Sem categoria', 'g', '4988.00', '500.00', 1),
(9, 'Lavanda', 'Sem categoria', 'g', '3000.00', '300.00', 1),
(10, 'Rooibos', 'Sem categoria', 'g', '4000.00', '400.00', 1),
(11, 'Maçã desidratada', 'Sem categoria', 'g', '3980.00', '400.00', 1),
(12, 'Alecrim', 'Sem categoria', 'g', '2992.00', '300.00', 1),
(13, 'Casca de limão', 'Sem categoria', 'g', '2990.00', '300.00', 1),
(14, 'Chá preto', 'Sem categoria', 'g', '5000.00', '500.00', 1),
(15, 'Canela', 'Sem categoria', 'g', '3000.00', '300.00', 1),
(16, 'Gengibre', 'Sem categoria', 'g', '3992.50', '400.00', 1),
(17, 'Cardamomo', 'Sem categoria', 'g', '2000.00', '200.00', 1),
(18, 'Cravo-da-índia', 'Sem categoria', 'g', '2000.00', '200.00', 1),
(19, 'Chá verde', 'Sem categoria', 'g', '4977.50', '500.00', 1),
(20, 'Casca de laranja', 'Sem categoria', 'g', '2985.00', '300.00', 1),
(21, 'po', 'Sem categoria', 'kg', '0.00', '10.00', 1),
(22, 'papa', 'Sem categoria', '50 kg', '0.00', '10.00', 0),
(23, 'papaa', 'Sem categoria', 'kg', '50.00', '5.00', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimentacoes_estoque`
--

CREATE TABLE `movimentacoes_estoque` (
  `id_movimentacao` int NOT NULL,
  `id_insumo` int DEFAULT NULL,
  `id_producao` int DEFAULT NULL,
  `id_produto` int DEFAULT NULL,
  `tipo` varchar(20) NOT NULL,
  `quantidade` decimal(12,2) NOT NULL,
  `data_movimentacao` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `observacoes` text,
  `estoque_anterior` decimal(12,2) DEFAULT NULL,
  `estoque_posterior` decimal(12,2) DEFAULT NULL,
  `motivo` varchar(150) DEFAULT NULL,
  `usuario` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `movimentacoes_estoque`
--

INSERT INTO `movimentacoes_estoque` (`id_movimentacao`, `id_insumo`, `id_producao`, `id_produto`, `tipo`, `quantidade`, `data_movimentacao`, `observacoes`, `estoque_anterior`, `estoque_posterior`, `motivo`, `usuario`) VALUES
(1, 8, 1, NULL, 'SAIDA', '12.00', '2026-11-16 22:02:40', 'Consumo na produção do lote teste1', NULL, NULL, NULL, NULL),
(2, 12, 1, NULL, 'SAIDA', '8.00', '2026-11-16 22:02:40', 'Consumo na produção do lote teste1', NULL, NULL, NULL, NULL),
(3, 13, 1, NULL, 'SAIDA', '10.00', '2026-11-16 22:02:40', 'Consumo na produção do lote teste1', NULL, NULL, NULL, NULL),
(4, 4, 2, NULL, 'SAIDA', '20.00', '2026-11-16 23:19:41', 'Consumo na produção do lote lote2teste', NULL, NULL, NULL, NULL),
(5, 6, 2, NULL, 'SAIDA', '20.00', '2026-11-16 23:19:42', 'Consumo na produção do lote lote2teste', NULL, NULL, NULL, NULL),
(6, 11, 2, NULL, 'SAIDA', '20.00', '2026-11-16 23:19:42', 'Consumo na produção do lote lote2teste', NULL, NULL, NULL, NULL),
(7, 23, NULL, NULL, 'Entrada', '50.00', '2026-11-23 03:49:38', 'Estoque inicial no cadastro do insumo', '0.00', '50.00', 'Entrada inicial', 'Sistema'),
(8, 16, 3, NULL, 'Saída', '7.50', '2026-11-23 04:43:01', 'Consumo de insumo na produção 3', '4000.00', '3992.50', 'Produção', 'Maria'),
(9, 19, 3, NULL, 'Saída', '22.50', '2026-11-23 04:43:02', 'Consumo de insumo na produção 3', '5000.00', '4977.50', 'Produção', 'Maria'),
(10, 20, 3, NULL, 'Saída', '15.00', '2026-11-23 04:43:02', 'Consumo de insumo na produção 3', '3000.00', '2985.00', 'Produção', 'Maria'),
(11, NULL, 3, 8, 'Entrada', '15.00', '2026-11-23 04:43:02', 'Entrada de produto acabado da produção 3', '0.00', '15.00', 'Produção', 'Maria'),
(12, NULL, NULL, 8, 'Saída', '-5.00', '2026-11-23 04:54:23', '', '15.00', '10.00', 'Venda loja física', 'Laura');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `parceiros`
--

CREATE TABLE `parceiros` (
  `id` int NOT NULL,
  `nome` varchar(120) NOT NULL,
  `cnpj_cpf` varchar(18) NOT NULL,
  `email` varchar(120) NOT NULL,
  `telefone` varchar(20) NOT NULL,
  `rua` varchar(120) DEFAULT NULL,
  `numero` varchar(10) DEFAULT NULL,
  `complemento` varchar(60) DEFAULT NULL,
  `bairro` varchar(80) DEFAULT NULL,
  `cidade` varchar(80) NOT NULL,
  `uf` char(2) NOT NULL,
  `cep` varchar(9) DEFAULT NULL,
  `situacao` enum('Ativo','Inativo') NOT NULL DEFAULT 'Ativo',
  `observacoes` text,
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `parceiros`
--

INSERT INTO `parceiros` (`id`, `nome`, `cnpj_cpf`, `email`, `telefone`, `rua`, `numero`, `complemento`, `bairro`, `cidade`, `uf`, `cep`, `situacao`, `observacoes`, `criado_em`) VALUES
(1, 'plo6', '561.845.784-66', 'teste@gmail.com', '(55) 6485-4587', NULL, NULL, NULL, NULL, 'foz', 'PR', NULL, 'Ativo', NULL, '2026-11-22 22:34:59');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `producao_insumos`
--

CREATE TABLE `producao_insumos` (
  `id_producao_insumo` int NOT NULL,
  `id_producao` int NOT NULL,
  `id_insumo` int NOT NULL,
  `quantidade_utilizada` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `producao_insumos`
--

INSERT INTO `producao_insumos` (`id_producao_insumo`, `id_producao`, `id_insumo`, `quantidade_utilizada`) VALUES
(1, 1, 8, '12.00'),
(2, 1, 12, '8.00'),
(3, 1, 13, '10.00'),
(4, 2, 4, '20.00'),
(5, 2, 6, '20.00'),
(6, 2, 11, '20.00'),
(7, 3, 16, '7.50'),
(8, 3, 19, '22.50'),
(9, 3, 20, '15.00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `producoes`
--

CREATE TABLE `producoes` (
  `id_producao` int NOT NULL,
  `id_produto` int NOT NULL,
  `id_receita` int NOT NULL,
  `quantidade` int NOT NULL,
  `lote` varchar(50) NOT NULL,
  `data_producao` date NOT NULL,
  `data_validade` date NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'Concluída',
  `usuario` varchar(100) DEFAULT NULL,
  `observacoes` text,
  `data_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `producoes`
--

INSERT INTO `producoes` (`id_producao`, `id_produto`, `id_receita`, `quantidade`, `lote`, `data_producao`, `data_validade`, `status`, `usuario`, `observacoes`, `data_registro`) VALUES
(1, 5, 5, 1, 'teste1', '2026-11-16', '2026-12-16', 'Concluída', 'Laura', '', '2026-11-16 22:02:39'),
(2, 9, 9, 2, 'lote2teste', '2026-11-16', '2027-11-16', 'Concluída', 'Laura', '', '2026-11-16 23:19:41'),
(3, 8, 8, 15, 'L003', '2026-11-23', '2027-05-22', 'Concluída', 'Maria', '', '2026-11-23 04:43:01');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `produtos`
--

CREATE TABLE `produtos` (
  `id_produto` int NOT NULL,
  `nome` varchar(150) NOT NULL,
  `categoria` varchar(100) DEFAULT NULL,
  `unidade_medida` varchar(20) DEFAULT 'un',
  `preco` decimal(10,2) DEFAULT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  `estoque_atual` decimal(12,2) NOT NULL DEFAULT '0.00',
  `estoque_minimo` decimal(12,2) NOT NULL DEFAULT '0.00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `produtos`
--

INSERT INTO `produtos` (`id_produto`, `nome`, `categoria`, `unidade_medida`, `preco`, `ativo`, `estoque_atual`, `estoque_minimo`) VALUES
(1, 'Blend Calme 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(2, 'Blend Felicitá 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(3, 'Blend Ormoni 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(4, 'Blend MaterniTea 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(5, 'Blend Airmid 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(6, 'Blend Chai Masala 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(7, 'Blend DesintoxiTea 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00'),
(8, 'Blend Animé 30g', 'Blend', 'un', NULL, 1, '10.00', '0.00'),
(9, 'Blend Amore 30g', 'Blend', 'un', NULL, 1, '0.00', '0.00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `receitas`
--

CREATE TABLE `receitas` (
  `id_receita` int NOT NULL,
  `id_produto` int NOT NULL,
  `nome` varchar(150) NOT NULL,
  `rendimento` decimal(10,2) NOT NULL DEFAULT '1.00',
  `unidade_rendimento` varchar(20) DEFAULT 'un',
  `descricao` text,
  `ativo` tinyint(1) DEFAULT '1',
  `rendimento_unidades` int NOT NULL DEFAULT '10',
  `rendimento_gramas` decimal(12,2) NOT NULL DEFAULT '300.00',
  `custo_total` decimal(12,2) NOT NULL DEFAULT '0.00',
  `validade_dias` int NOT NULL DEFAULT '180'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `receitas`
--

INSERT INTO `receitas` (`id_receita`, `id_produto`, `nome`, `rendimento`, `unidade_rendimento`, `descricao`, `ativo`, `rendimento_unidades`, `rendimento_gramas`, `custo_total`, `validade_dias`) VALUES
(1, 1, 'Receita Blend Calme 30g', '10.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(2, 2, 'Receita Blend Felicitá 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(3, 3, 'Receita Blend Ormoni 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(4, 4, 'Receita Blend MaterniTea 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(5, 5, 'Receita Blend Airmid 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(6, 6, 'Receita Blend Chai Masala 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(7, 7, 'Receita Blend DesintoxiTea 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(8, 8, 'Receita Blend Animé 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(9, 9, 'Receita Blend Amore 30g', '1.00', 'un', 'Receita fictícia para produção de uma embalagem de 30g', 1, 10, '300.00', '0.00', 180),
(17, 5, 'sss', '10.00', 'un', NULL, 1, 10, '300.00', '25.00', 180);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `receita_insumos`
--

CREATE TABLE `receita_insumos` (
  `id_receita_insumo` int NOT NULL,
  `id_receita` int NOT NULL,
  `id_insumo` int NOT NULL,
  `quantidade` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `receita_insumos`
--

INSERT INTO `receita_insumos` (`id_receita_insumo`, `id_receita`, `id_insumo`, `quantidade`) VALUES
(1, 4, 1, '5.00'),
(4, 7, 3, '8.00'),
(6, 9, 4, '10.00'),
(7, 7, 4, '10.00'),
(8, 2, 4, '12.00'),
(9, 2, 5, '10.00'),
(10, 9, 6, '10.00'),
(11, 2, 6, '8.00'),
(12, 3, 7, '12.00'),
(13, 5, 8, '12.00'),
(14, 3, 8, '10.00'),
(15, 3, 9, '8.00'),
(16, 4, 10, '15.00'),
(17, 9, 11, '10.00'),
(18, 4, 11, '10.00'),
(19, 5, 12, '8.00'),
(20, 5, 13, '10.00'),
(21, 6, 14, '15.00'),
(22, 6, 15, '6.00'),
(23, 8, 16, '5.00'),
(24, 6, 16, '5.00'),
(25, 6, 17, '3.00'),
(26, 6, 18, '1.00'),
(27, 8, 19, '15.00'),
(28, 7, 19, '12.00'),
(29, 8, 20, '10.00'),
(34, 1, 1, '12.00'),
(35, 1, 3, '8.00'),
(36, 1, 2, '10.00'),
(37, 1, 12, '1.00'),
(38, 17, 3, '4.00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `saidas`
--

CREATE TABLE `saidas` (
  `id` int NOT NULL,
  `produto_id` int NOT NULL,
  `quantidade` decimal(12,3) NOT NULL,
  `motivo` varchar(60) NOT NULL,
  `destino` enum('Cliente','Parceiro','E-commerce') NOT NULL,
  `cliente_id` int DEFAULT NULL,
  `fornecedor_id` int DEFAULT NULL,
  `parceiro_id` int DEFAULT NULL,
  `valor_unitario` decimal(12,2) DEFAULT NULL,
  `valor_total` decimal(12,2) DEFAULT NULL,
  `usuario_id` int NOT NULL,
  `data_saida` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `observacoes` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `saidas`
--

INSERT INTO `saidas` (`id`, `produto_id`, `quantidade`, `motivo`, `destino`, `cliente_id`, `fornecedor_id`, `parceiro_id`, `valor_unitario`, `valor_total`, `usuario_id`, `data_saida`, `observacoes`) VALUES
(1, 8, '5.000', 'Venda loja física', 'Cliente', NULL, NULL, NULL, NULL, NULL, 1, '2026-11-23 01:54:23', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int NOT NULL,
  `nome` varchar(100) NOT NULL,
  `email` varchar(120) NOT NULL,
  `senha_hash` varchar(255) NOT NULL,
  `perfil` enum('Administrador','Operador') NOT NULL DEFAULT 'Operador',
  `situacao` enum('Ativo','Inativo') NOT NULL DEFAULT 'Ativo',
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nome`, `email`, `senha_hash`, `perfil`, `situacao`, `criado_em`) VALUES
(1, 'Laura', 'laura@chasdalaura.exemplo', 'SEM_LOGIN', 'Administrador', 'Ativo', '2026-11-23 04:47:15'),
(2, 'Matheus', 'matheus@chasdalaura.exemplo', 'SEM_LOGIN', 'Administrador', 'Ativo', '2026-11-23 04:47:15');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_categoria` (`nome`,`tipo`);

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cpf_cnpj` (`cpf_cnpj`);

--
-- Indices de la tabla `fornecedores`
--
ALTER TABLE `fornecedores`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cnpj` (`cnpj`);

--
-- Indices de la tabla `insumos`
--
ALTER TABLE `insumos`
  ADD PRIMARY KEY (`id_insumo`);

--
-- Indices de la tabla `movimentacoes_estoque`
--
ALTER TABLE `movimentacoes_estoque`
  ADD PRIMARY KEY (`id_movimentacao`),
  ADD KEY `fk_mov_insumo` (`id_insumo`),
  ADD KEY `fk_mov_producao` (`id_producao`);

--
-- Indices de la tabla `parceiros`
--
ALTER TABLE `parceiros`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cnpj_cpf` (`cnpj_cpf`);

--
-- Indices de la tabla `producao_insumos`
--
ALTER TABLE `producao_insumos`
  ADD PRIMARY KEY (`id_producao_insumo`),
  ADD KEY `fk_pi_producao` (`id_producao`),
  ADD KEY `fk_pi_insumo` (`id_insumo`);

--
-- Indices de la tabla `producoes`
--
ALTER TABLE `producoes`
  ADD PRIMARY KEY (`id_producao`),
  ADD KEY `fk_producao_produto` (`id_produto`),
  ADD KEY `fk_producao_receita` (`id_receita`);

--
-- Indices de la tabla `produtos`
--
ALTER TABLE `produtos`
  ADD PRIMARY KEY (`id_produto`);

--
-- Indices de la tabla `receitas`
--
ALTER TABLE `receitas`
  ADD PRIMARY KEY (`id_receita`),
  ADD KEY `fk_receita_produto` (`id_produto`);

--
-- Indices de la tabla `receita_insumos`
--
ALTER TABLE `receita_insumos`
  ADD PRIMARY KEY (`id_receita_insumo`),
  ADD UNIQUE KEY `uq_receita_insumo` (`id_receita`,`id_insumo`),
  ADD KEY `fk_ri_insumo` (`id_insumo`);

--
-- Indices de la tabla `saidas`
--
ALTER TABLE `saidas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_saida_produto` (`produto_id`),
  ADD KEY `fk_saida_cliente` (`cliente_id`),
  ADD KEY `fk_saida_fornecedor` (`fornecedor_id`),
  ADD KEY `fk_saida_parceiro` (`parceiro_id`),
  ADD KEY `fk_saida_usuario` (`usuario_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `fornecedores`
--
ALTER TABLE `fornecedores`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `insumos`
--
ALTER TABLE `insumos`
  MODIFY `id_insumo` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de la tabla `movimentacoes_estoque`
--
ALTER TABLE `movimentacoes_estoque`
  MODIFY `id_movimentacao` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT de la tabla `parceiros`
--
ALTER TABLE `parceiros`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `producao_insumos`
--
ALTER TABLE `producao_insumos`
  MODIFY `id_producao_insumo` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `producoes`
--
ALTER TABLE `producoes`
  MODIFY `id_producao` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `produtos`
--
ALTER TABLE `produtos`
  MODIFY `id_produto` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `receitas`
--
ALTER TABLE `receitas`
  MODIFY `id_receita` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT de la tabla `receita_insumos`
--
ALTER TABLE `receita_insumos`
  MODIFY `id_receita_insumo` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT de la tabla `saidas`
--
ALTER TABLE `saidas`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `movimentacoes_estoque`
--
ALTER TABLE `movimentacoes_estoque`
  ADD CONSTRAINT `fk_mov_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumos` (`id_insumo`),
  ADD CONSTRAINT `fk_mov_producao` FOREIGN KEY (`id_producao`) REFERENCES `producoes` (`id_producao`);

--
-- Filtros para la tabla `producao_insumos`
--
ALTER TABLE `producao_insumos`
  ADD CONSTRAINT `fk_pi_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumos` (`id_insumo`),
  ADD CONSTRAINT `fk_pi_producao` FOREIGN KEY (`id_producao`) REFERENCES `producoes` (`id_producao`);

--
-- Filtros para la tabla `producoes`
--
ALTER TABLE `producoes`
  ADD CONSTRAINT `fk_producao_produto` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`),
  ADD CONSTRAINT `fk_producao_receita` FOREIGN KEY (`id_receita`) REFERENCES `receitas` (`id_receita`);

--
-- Filtros para la tabla `receitas`
--
ALTER TABLE `receitas`
  ADD CONSTRAINT `fk_receita_produto` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`);

--
-- Filtros para la tabla `receita_insumos`
--
ALTER TABLE `receita_insumos`
  ADD CONSTRAINT `fk_ri_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumos` (`id_insumo`),
  ADD CONSTRAINT `fk_ri_receita` FOREIGN KEY (`id_receita`) REFERENCES `receitas` (`id_receita`);

--
-- Filtros para la tabla `saidas`
--
ALTER TABLE `saidas`
  ADD CONSTRAINT `fk_saida_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  ADD CONSTRAINT `fk_saida_fornecedor` FOREIGN KEY (`fornecedor_id`) REFERENCES `fornecedores` (`id`),
  ADD CONSTRAINT `fk_saida_parceiro` FOREIGN KEY (`parceiro_id`) REFERENCES `parceiros` (`id`),
  ADD CONSTRAINT `fk_saida_produto` FOREIGN KEY (`produto_id`) REFERENCES `produtos` (`id_produto`),
  ADD CONSTRAINT `fk_saida_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
