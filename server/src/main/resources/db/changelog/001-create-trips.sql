--liquibase formatted sql
--changeset travel-planner:1

CREATE TABLE trips (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL
);
