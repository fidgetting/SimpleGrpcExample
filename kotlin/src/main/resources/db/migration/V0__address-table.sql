
CREATE TABLE address (
    id       bigserial not null primary key,
    address  jsonb not null,
    added_at timestamp not null default now()
);

ALTER SEQUENCE address_id_seq RESTART WITH 1;
