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

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * ValueObject für einen neuen Fahrzeugen mit den Benutzerdaten an der REST-Schnittstelle.
 * Beim Lesen wird die Klasse [FahrzeugModel] für die Ausgabe verwendet, d.h. ohne die Benutzerdaten.

 * @property fahrzeugDTO Das Fahrzeug.
 * @property userDTO Die Benutzerdaten
 */
data class FahrzeugUserDTO(
    @JsonProperty("fahrzeug")
    val fahrzeugDTO: FahrzeugDTO,

    @JsonProperty("user")
    val userDTO: CustomUserDTO,
) {
    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Fahrzeug-) Objekt die gleiche email hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FahrzeugUserDTO
        return fahrzeugDTO.kennzeichen == other.fahrzeugDTO.kennzeichen
    }

    /**
     * Hashwert aufgrund der email.
     * @return Der Hashwert.
     */
    override fun hashCode() = fahrzeugDTO.kennzeichen.hashCode()
}
