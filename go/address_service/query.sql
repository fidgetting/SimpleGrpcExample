-- name: GetAddress :one
SELECT *
FROM address
WHERE id = $1;

-- name: CreateAddress :one
INSERT INTO address (address)
VALUES ($1)
RETURNING *;

-- name: UpsertAddress :one
INSERT INTO address (id, address)
VALUES ($1, $2)
ON CONFLICT (id) DO UPDATE
    SET address = excluded.address
RETURNING *;

-- name: RemoveAddress :one
DELETE FROM address
WHERE id = $1
RETURNING *;
