--liquibase formatted sql
--changeset travel-planner:2

CREATE TABLE routes (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    points JSONB NOT NULL,
    preview JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX routes_trip_id_idx ON routes(trip_id);
