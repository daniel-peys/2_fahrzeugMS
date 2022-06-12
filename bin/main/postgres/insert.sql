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

--  docker compose exec postgres bash
--  psql --dbname=kunde --username=kunde --file=/scripts/insert.sql

INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000000','admin','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');
INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000001','alpha','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');
INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000002','alpha2','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');
INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000030','alpha3','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');
INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000040','delta','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');
INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000050','epsilon','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');
INSERT INTO login (id, username, password) VALUES ('30000000-0000-0000-0000-000000000060','phi','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$iE4+NpU8xcjEu8z2YEtjYw$DbmfrGjL6ac04HGHQ0tdng6vxg3OG/A+GSY3WVUdbNU');

INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000000','ADMIN');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000000','KUNDE');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000000','ACTUATOR');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000001','KUNDE');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000002','KUNDE');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000030','KUNDE');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000040','FAHRZEUG');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000050','FAHRZEUG');
INSERT INTO login_rollen (login_id, rolle) VALUES ('30000000-0000-0000-0000-000000000060','FAHRZEUG');

INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000000','Hans','Peter');
INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000010','Hans','Peter');
INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000020','Hans','Peter');
INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000030','Hans','Peter');
INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000040','Hans','Peter');
INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000050','Hans','Peter');
INSERT INTO fahrzeughalter (id, vorname, nachname) VALUES ('20000000-0000-0000-0000-000000000060','Hans','Peter');

-- admin
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000000',0,'Admin','KA N 0',0,'2021-01-31','A','20000000-0000-0000-0000-000000000000','admin','2021-01-31 00:00:00','2021-01-31 00:00:00');
-- HTTP GET
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000010',0,'Felali','KA N 1',1,'2021-01-01','P','20000000-0000-0000-0000-000000000010','alpha','2021-01-31 00:00:00','2021-01-31 00:00:00');
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000020',0,'Trekker','KA N 2',2,'2021-01-02','N','20000000-0000-0000-0000-000000000020','phi','2021-01-31 00:00:00','2021-01-31 00:00:00');
-- HTTP PUT
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000030',0,'Pferdehenger','KA N 3',3,'2021-01-03','A','20000000-0000-0000-0000-000000000030','alpha2','2021-01-31 00:00:00','2021-01-31 00:00:00');
-- HTTP PATCH
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000040',0,'Popel','KA N 4',4,'2021-01-04','P','20000000-0000-0000-0000-000000000040','delta','2021-01-31 00:00:00','2021-01-31 00:00:00');
-- HTTP DELETE
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000050',0,'Riesenhenger','KA N 5',5,'2021-01-05','A','20000000-0000-0000-0000-000000000050','delta','2021-01-31 00:00:00','2021-01-31 00:00:00');
-- zur freien Verfuegung
INSERT INTO fahrzeug (id, version, beschreibung, kennzeichen, kilometerstand, erstzulassung, fahrzeugtyp, fahrzeughalter_id, username, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000060',0,'Kran','KA N 6',6,'2021-01-06','N','20000000-0000-0000-0000-000000000060','delta','2021-01-31 00:00:00','2021-01-31 00:00:00');
