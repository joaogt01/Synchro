CREATE TABLE disponibilidade(
    id BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL REFERENCES profissionais(id),
    dia_da_semana SMALLINT NOT NULL,
    inicio TIME NOT NULL,
    fim TIME NOT NULL,
    CHECK ( inicio < fim )
);

CREATE TABLE slots_bloqueados (
    id BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL REFERENCES profissionais(id),
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    CHECK ( inicio < fim )
);