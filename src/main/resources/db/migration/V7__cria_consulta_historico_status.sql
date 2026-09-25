CREATE TABLE consulta_historico_status (
    id BIGSERIAL PRIMARY KEY,
    consulta_id BIGINT NOT NULL REFERENCES consulta(id),
    status_anterior VARCHAR(20),
    status_novo VARCHAR(20) NOT NULL,
    alterado_por_usuario_id BIGINT REFERENCES usuarios(id),
    alterado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_consulta_historico_consulta ON consulta_historico_status (consulta_id);