CREATE TABLE profissionais (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    especialidade VARCHAR(100) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE pacientes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    telefone VARCHAR(20)
);