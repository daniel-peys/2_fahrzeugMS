-- Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

-- docker compose exec postgres bash
-- psql --dbname=fahrzeug --username=fahrzeug --file=/scripts/create.sql

-- https://www.postgresql.org/docs/devel/app-psql.html
-- https://www.postgresql.org/docs/current/ddl-schemas.html
-- https://www.postgresql.org/docs/current/ddl-schemas.html#DDL-SCHEMAS-CREATE
-- "user-private schema" (Default-Schema: public)
CREATE SCHEMA IF NOT EXISTS AUTHORIZATION fahrzeug;

ALTER ROLE fahrzeug SET search_path = 'fahrzeug';

-- https://www.postgresql.org/docs/current/sql-createtable.html
-- https://www.postgresql.org/docs/current/datatype.html
-- BEACHTE: user ist ein Schluesselwort
CREATE TABLE IF NOT EXISTS login (
             -- https://www.postgresql.org/docs/current/datatype-uuid.html
             -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-PRIMARY-KEYS
             -- impliziter Index fuer Primary Key
    id       uuid PRIMARY KEY,
    username varchar(20) UNIQUE NOT NULL,
    password varchar(150) NOT NULL
) TABLESPACE fahrzeugspace;

CREATE TABLE IF NOT EXISTS login_rollen (
             -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-FK
    login_id uuid NOT NULL REFERENCES login,
             -- https://www.postgresql.org/docs/current/ddl-constraints.html#id-1.5.4.6.6
             -- https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-POSIX-REGEXP
    rolle    varchar(20) NOT NULL CHECK (rolle ~ 'ADMIN|KUNDE|ACTUATOR|FAHRZEUG'),

    PRIMARY KEY (login_id, rolle)
) TABLESPACE fahrzeugspace;

-- https://www.postgresql.org/docs/docs/sql-createindex.html
CREATE INDEX IF NOT EXISTS login_rollen_idx ON login_rollen(login_id) TABLESPACE fahrzeugspace;

CREATE TABLE IF NOT EXISTS fahrzeughalter (
    id    uuid PRIMARY KEY,
    vorname   char(50) NOT NULL,
    nachname   varchar(40) NOT NULL
) TABLESPACE fahrzeugspace;

-- default: btree
CREATE INDEX IF NOT EXISTS fahrzeughalter_vorname_idx ON fahrzeughalter(vorname) TABLESPACE fahrzeugspace;

CREATE TABLE IF NOT EXISTS fahrzeug (
    id            uuid PRIMARY KEY,
                  -- https://www.postgresql.org/docs/current/datatype-numeric.html#DATATYPE-INT
    version       integer NOT NULL DEFAULT 0,
    beschreibung      varchar(40) NOT NULL,
                  -- impliziter Index als B-Baum durch UNIQUE
                  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-UNIQUE-CONSTRAINTS
    kennzeichen         varchar(40) UNIQUE NOT NULL,
                  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-CHECK-CONSTRAINTS
    kilometerstand     integer NOT NULL CHECK (kilometerstand >= 0 AND kilometerstand <= 999999999),
    erstzulassung  date, --CHECK (erstzulassung <= current_date),
    fahrzeugtyp    char(1) CHECK (fahrzeugtyp ~ 'A|N|P'),
    fahrzeughalter_id    uuid NOT NULL REFERENCES fahrzeughalter,
    username      varchar(20) NOT NULL REFERENCES login(username),
                  -- https://www.postgresql.org/docs/current/datatype-datetime.html
    erzeugt       timestamp NOT NULL,
    aktualisiert  timestamp NOT NULL
) TABLESPACE fahrzeugspace;

CREATE INDEX IF NOT EXISTS fahrzeug_beschreibung_idx ON fahrzeug(beschreibung) TABLESPACE fahrzeugspace;
