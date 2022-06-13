/*
 * Copyright (C) 2021 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.fahrzeug.rest

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugType
import com.acme.fahrzeug.entity.Fahrzeughalter
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

/**
 * Unveränderliche Daten eines Fahrzeuges an der REST-Schnittstelle ohne die Benutzerdaten.
 * Diese Daten werden beim Lesen angezeigt und können überschrieben werden.
 *
 * @property beschreibung Gültige Beschreibung eines Fahrzeuges, d.h. mit einem geeigneten Muster.
 * @property kennzeichen Kennzeichen eines Fahrzeuges
 * @property kilometerstand Kilometerstand eines Fahrzeuges mit eingeschränkten Werten.
 * @property erstzulassung Die Erstzulassung eines Fahrzeuges
 * @property fahrzeugtype Der Fahrzeugtyp eines Fahrzeuges.
 * @property fahrzeughalter Der Fahrzeughalter eines Fahrzeuges
 */
@JsonPropertyOrder(
    "beschreibung", "kennzeichen", "kilometerstand", "erstzulassung", "fahrzeugtyp", "fahrzeughalter",
)
@Relation(collectionRelation = "fahrzeuge", itemRelation = "fahrzeug")
data class FahrzeugModel(
    val beschreibung: String,

    val kennzeichen: String,

    val kilometerstand: Int = 0,

    val erstzulassung: LocalDate?,

    val fahrzeugtype: FahrzeugType?,

    val fahrzeughalter: Fahrzeughalter,
) : RepresentationModel<FahrzeugModel>() {
    constructor(fahrzeug: Fahrzeug) : this(
        fahrzeug.beschreibung,
        fahrzeug.kennzeichen,
        fahrzeug.kilometerstand,
        fahrzeug.erstzulassung,
        fahrzeug.fahrzeugtyp,
        fahrzeug.fahrzeughalter,
    )

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Fahrzeug-) Objekt das gleiche Kennnzeichen hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FahrzeugModel
        return kennzeichen == other.kennzeichen
    }

    /**
     * Hashwert aufgrund der email.
     * @return Der Hashwert.
     */
    override fun hashCode() = kennzeichen.hashCode()
}
