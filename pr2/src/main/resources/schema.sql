CREATE TABLE IF NOT EXISTS books (
    id          UUID         NOT NULL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    author      VARCHAR(100) NOT NULL,
    genre       VARCHAR(50)  NOT NULL,
    publication_year INT     NOT NULL,
    price       DOUBLE       NOT NULL,
    pages       INT          NOT NULL,
    status      VARCHAR(20)  NOT NULL
);
