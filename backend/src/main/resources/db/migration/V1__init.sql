-- =========================================
-- V1__init.sql - Criação inicial do schema
-- =========================================

-- Extension para UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================
-- TABELA: usuarios
-- =========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(100)    NOT NULL,
    cpf             VARCHAR(11)     NOT NULL,
    data_nascimento DATE            NOT NULL,
    senha           VARCHAR(255)    NOT NULL,
    tipo            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT usuarios_tipo_check   CHECK (tipo   IN ('ADMIN', 'USER')),
    CONSTRAINT usuarios_status_check CHECK (status IN ('ATIVO', 'INATIVO'))
);

-- Índices da tabela usuarios
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_cpf     ON usuarios(cpf);
CREATE        INDEX IF NOT EXISTS idx_usuario_tipo    ON usuarios(tipo);
CREATE        INDEX IF NOT EXISTS idx_usuario_status  ON usuarios(status);

-- =========================================
-- TABELA: enderecos
-- =========================================
CREATE TABLE IF NOT EXISTS enderecos (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cep         VARCHAR(8)  NOT NULL,
    logradouro  VARCHAR(255)NOT NULL,
    numero      VARCHAR(20) NOT NULL,
    complemento VARCHAR(100),
    bairro      VARCHAR(100)NOT NULL,
    cidade      VARCHAR(100)NOT NULL,
    estado      VARCHAR(2)  NOT NULL,
    principal   BOOLEAN     NOT NULL DEFAULT FALSE,
    usuario_id  UUID        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices da tabela enderecos
CREATE INDEX IF NOT EXISTS idx_endereco_usuario   ON enderecos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_endereco_cep       ON enderecos(cep);
CREATE INDEX IF NOT EXISTS idx_endereco_cidade_uf ON enderecos(cidade, estado);

-- Garante apenas um endereço principal por usuário (partial unique index)
CREATE UNIQUE INDEX IF NOT EXISTS idx_endereco_unico_principal
    ON enderecos(usuario_id)
    WHERE principal = TRUE;

-- =========================================
-- TRIGGERS: updated_at automático
-- =========================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enderecos_updated_at
    BEFORE UPDATE ON enderecos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =========================================
-- SEED DATA (desenvolvimento)
-- =========================================

-- Admin: CPF 52998224725 / senha: Admin123!
INSERT INTO usuarios (nome, cpf, data_nascimento, senha, tipo, status)
VALUES (
    'Administrador do Sistema',
    '52998224725',
    '1990-01-01',
    '$2a$10$vo9AOP.q2QQ5YbG6euJ.AebzvGHk2HUL8PJr67FfMrN/F7Rw4rEiS',
    'ADMIN',
    'ATIVO'
) ON CONFLICT (cpf) DO NOTHING;

-- User: CPF 39053344705 / senha: User123!
INSERT INTO usuarios (nome, cpf, data_nascimento, senha, tipo, status)
VALUES (
    'Usuário de Teste',
    '39053344705',
    '1995-05-15',
    '$2a$10$8NJ2vVg7rjK1/5Gv84/VL.A1L7nu1waciCMnVMln.J5trCqrehgpG',
    'USER',
    'ATIVO'
) ON CONFLICT (cpf) DO NOTHING;

-- Endereço principal do admin
INSERT INTO enderecos (cep, logradouro, numero, complemento, bairro, cidade, estado, principal, usuario_id)
SELECT
    '01310100', 'Avenida Paulista', '1000', 'Sala 100',
    'Bela Vista', 'São Paulo', 'SP', TRUE, id
FROM usuarios WHERE cpf = '52998224725'
ON CONFLICT DO NOTHING;

-- Endereço secundário do admin
INSERT INTO enderecos (cep, logradouro, numero, bairro, cidade, estado, principal, usuario_id)
SELECT
    '20040002', 'Avenida Rio Branco', '50',
    'Centro', 'Rio de Janeiro', 'RJ', FALSE, id
FROM usuarios WHERE cpf = '52998224725'
ON CONFLICT DO NOTHING;
