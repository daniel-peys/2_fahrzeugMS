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
import java.time.LocalDate

/**
 * ValueObject für das Ändern eines neuen Fahrzeuges. Beim Lesen wird die Klasse [FahrzeugModel]
 * für die Ausgabe verwendet und für das Neuanlegen die Klasse [FahrzeugUserDTO].
 *
 * @property beschreibung Gültiger Beshcreibung eines Fahrzeug, d.h. mit einem geeigneten Muster.
 * @property kennzeichen Kennzeichen eines Fahrzeuges.
 * @property kilometerstand Kilometerstand eines Fahrzeuges mit eingeschränkten Werten.
 * @property erstzulassung Das Erstzulassung eines Fahrzeuges.
 * @property fahrzeugtype Das Fahrzeugtyp eines Fahrzeuges
 * @property fahrzeughalter Der Fahrzeughalter eines Fahrzeuges
 */
data class FahrzeugDTO(
    val beschreibung: String,

    val kennzeichen: String,

    val kilometerstand: Int = 0,

    val erstzulassung: LocalDate?,

    val fahrzeugtype: FahrzeugType?,

    val fahrzeughalter: Fahrzeughalter,
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns
     * @return Fahrzeugobjekt für den Anwendungskern
     */
    fun toFahrzeug() = Fahrzeug(
        id = null,
        beschreibung = beschreibung,
        kennzeichen = kennzeichen,
        kilometerstand = kilometerstand,
        erstzulassung = erstzulassung,
        fahrzeugtyp = fahrzeugtype,
        fahrzeughalter = fahrzeughalter,
        username = "TBD",
    )

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende Fahrzeug-Objekt das gleiche Kennzeichen hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FahrzeugDTO
        return kennzeichen == other.kennzeichen
    }

    /**
     * Hashwert aufgrund des Kennzeichens
     * @return Der Hashwert.
     */
    override fun hashCode() = kennzeichen.hashCode()
}
