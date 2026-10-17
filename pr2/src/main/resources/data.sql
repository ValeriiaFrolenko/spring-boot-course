-- Пароль (admin та user) "password"

INSERT INTO users (username, password, enabled) VALUES
        ('admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', true),
        ('user', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', true);

INSERT INTO authorities (username, authority) VALUES
      ('admin', 'ROLE_ADMIN'),
      ('admin', 'ROLE_USER'),
      ('user', 'ROLE_USER');