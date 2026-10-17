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

CREATE TABLE IF NOT EXISTS users (
     username VARCHAR(50) NOT NULL PRIMARY KEY,
     password VARCHAR(500) NOT NULL,
     enabled  BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS authorities (
        username  VARCHAR(50) NOT NULL,
       authority VARCHAR(50) NOT NULL,
       CONSTRAINT fk_authorities_users FOREIGN KEY(username) REFERENCES users(username)
);