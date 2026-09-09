CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE html_templates (
                                id          BIGSERIAL PRIMARY KEY,
                                user_id     BIGINT NOT NULL REFERENCES users(id),
                                name        VARCHAR(200) NOT NULL,
                                raw_html    TEXT NOT NULL,
                                created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE campaigns (
                           id          BIGSERIAL PRIMARY KEY,
                           user_id     BIGINT NOT NULL REFERENCES users(id),
                           name        VARCHAR(200) NOT NULL,
                           description TEXT,
                           created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE forms (
                       id            BIGSERIAL PRIMARY KEY,
                       campaign_id   BIGINT NOT NULL REFERENCES campaigns(id),
                       template_id   BIGINT NOT NULL REFERENCES html_templates(id),
                       name          VARCHAR(200) NOT NULL,
                       field_schema  JSONB NOT NULL,
                       status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE links (
                       id          BIGSERIAL PRIMARY KEY,
                       form_id     BIGINT NOT NULL REFERENCES forms(id),
                       channel     VARCHAR(20) NOT NULL,
                       code        VARCHAR(32) NOT NULL UNIQUE,
                       created_at  TIMESTAMP NOT NULL DEFAULT now(),
                       CONSTRAINT uq_form_channel UNIQUE (form_id, channel)
);

CREATE TABLE visits (
                        id          BIGSERIAL PRIMARY KEY,
                        form_id     BIGINT NOT NULL REFERENCES forms(id),
                        link_id     BIGINT REFERENCES links(id),
                        channel     VARCHAR(20),
                        visitor_id  VARCHAR(64) NOT NULL,
                        ip          VARCHAR(64),
                        user_agent  TEXT,
                        referrer    TEXT,
                        created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE leads (
                       id          BIGSERIAL PRIMARY KEY,
                       form_id     BIGINT NOT NULL REFERENCES forms(id),
                       link_id     BIGINT REFERENCES links(id),
                       channel     VARCHAR(20),
                       visitor_id  VARCHAR(64),
                       payload     JSONB NOT NULL,
                       created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_visits_form    ON visits(form_id, channel);
CREATE INDEX idx_visits_visitor ON visits(form_id, visitor_id);
CREATE INDEX idx_leads_form     ON leads(form_id, channel);