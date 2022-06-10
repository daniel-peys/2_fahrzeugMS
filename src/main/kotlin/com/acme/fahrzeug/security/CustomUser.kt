/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.fahrzeug.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.util.ReflectionUtils.findField
import org.springframework.util.ReflectionUtils.makeAccessible
import org.springframework.util.ReflectionUtils.setField

/**
 * Klasse für Benutzerdaten für _Spring Security_.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property username Benutzername
 * @property password Passwort
 * @property authorities Rollen
 */
class CustomUser(
    username: String,
    password: String,
    authorities: Collection<GrantedAuthority> = setOfNotNull(SimpleGrantedAuthority(Rolle.kundeStr)),
) : User(username, password, authorities) {
    /**
     * Konstruktor für Spring Data, weil die geerbten Java-Attribute `final` sind.
     * @param username Benutzer- bzw. Loginname
     * @param password Passwort
     * @param enabled Ist die Benutzerkennung schon benutzbar?
     * @param accountNonExpired Ist die Benutzerkennung noch nicht abgelaufen?
     * @param credentialsNonExpired ISt das Passwort noch nicht abgelaufen?
     * @param accountNonLocked Ist der Acccount nicht gesperrt?
     * @param authorities Berechtigungen bzw. Rollen
     */
    // enabled, accoutNonExpired, ... aus der Klasse "User" sind private final
    @Suppress("unused", "LongParameterList")
    constructor(
        username: String,
        password: String,
        enabled: Boolean,
        accountNonExpired: Boolean,
        credentialsNonExpired: Boolean,
        accountNonLocked: Boolean,
        authorities: Collection<SimpleGrantedAuthority>,
    ) : this(username = username, password = password, authorities = authorities) {
        setFinalField("enabled", enabled)
        setFinalField("accountNonExpired", accountNonExpired)
        setFinalField("credentialsNonExpired", credentialsNonExpired)
        setFinalField("accountNonLocked", accountNonLocked)
    }

    /**
     * Ein CustomUser-Objekt als String, z.B. für Logging.
     * @return String mit den Properties.
     */
    override fun toString() =
        "CustomUser(super=${super.toString()})"

    private fun setFinalField(fieldName: String, value: Any) {
        val field = findField(User::class.java, fieldName) ?: return
        makeAccessible(field)
        setField(field, this, value)
    }
}
