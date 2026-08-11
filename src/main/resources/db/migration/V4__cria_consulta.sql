CREATE TABLE consulta (
    id BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL REFERENCES profissionais(id),
    paciente_id BIGINT NOT NULL REFERENCES pacientes(id),
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    CHECK ( inicio < fim ),
    CONSTRAINT uq_profissional_slot UNIQUE (profissional_id, inicio)
);