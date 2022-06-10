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

import com.acme.fahrzeug.security.CustomUser

/**
 * ValueObject für die Benutzerdaten, wenn ein neuer Fahrzueg per POST angelegt wird.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property username Benutzername.
 * @property password Passwort.
 */
data class CustomUserDTO(val username: String, val password: String) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns
     * @return Objekt für den Anwendungskern
     */
    fun toCustomUser() = CustomUser(username = username, password = password)

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende Objekt die gleichen Benutzernamen hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomUserDTO
        return username == other.username
    }

    /**
     * Hashwert aufgrund des Benutzernamens.
     * @return Der Hashwert.
     */
    override fun hashCode() = username.hashCode()
}
