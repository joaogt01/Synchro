CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE consulta DROP CONSTRAINT IF EXISTS uq_profissional_slot;

ALTER TABLE consulta
    ADD CONSTRAINT excl_profissional_overlap
    EXCLUDE USING gist (
        profissional_id WITH =,
        tsrange(inicio, fim) WITH &&
    )
    WHERE (status <> 'CANCELADO');