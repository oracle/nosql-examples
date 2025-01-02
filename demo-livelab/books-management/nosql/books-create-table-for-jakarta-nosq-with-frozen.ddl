CREATE TABLE if not exists books (
    id STRING,
    entity STRING,
    content JSON,
    primary key (id)
)
WITH SCHEMA FROZEN
