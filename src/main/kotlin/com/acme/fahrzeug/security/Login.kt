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

import com.acme.fahrzeug.entity.DbId
import org.springframework.security.core.authority.SimpleGrantedAuthority
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.NamedQuery
import javax.persistence.Table

/**
 * Entity-Klasse, um Benutzerkennungen bestehend aus Benutzername,
 * Passwort und Rollen zu repräsentieren, die in der DB verwaltet
 * werden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @param id ID der Benutzerkennung in der DB
 * @param username Benutzer- bzw. Loginname
 * @param password Passwort
 * @param rollen Berechtigungen bzw. Rollen
 *
 */
@Entity
@Table(name = "login")
@NamedQuery(
    name = Login.BY_USERNAME,
    query = "SELECT login " +
        "FROM  Login login " +
        "WHERE login.username = :${Login.PARAM_USERNAME}",
)
@NamedQuery(
    name = Login.USERNAME_EXISTS,
    query = "SELECT COUNT(*) " +
        "FROM  Login login " +
        "WHERE login.username = :${Login.PARAM_USERNAME}",
)
data class Login(
    @Id
    @GeneratedValue
    // Oracle: https://stackoverflow.com/questions/50003906/storing-uuid-as-string-in-mysql-using-jpa
    // @GeneratedValue(generator = "UUID")
    // @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    // @Column(columnDefinition = "char(36)")
    // @Type(type = "org.hibernate.type.UUIDCharType")
    @Suppress("experimental:annotation-spacing")
    val id: DbId? = null,

    val username: String,
    val password: String,

    @ElementCollection(fetch = EAGER)
    @CollectionTable
    @Column(name = "rolle")
    val rollen: Set<String>?,
) {
    /**
     * Konvertierungsfunktion, um ein User-Objekt aus der DB in ein User-Objekt für Spring Security zu konvertieren.
     *
     * @return Ein Objekt von [CustomUser] für Spring Security
     */
    fun toCustomUser() = CustomUser(
        username = username,
        password = password,
        enabled = true,
        accountNonExpired = true,
        credentialsNonExpired = true,
        accountNonLocked = true,
        authorities = rollen
            ?.map { rolle -> SimpleGrantedAuthority("${Rolle.rolePrefix}$rolle") }
            ?: emptySet(),
    )

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Fahrzeug-) Objekt die gleiche ID hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Login
        return id != null && id == other.id
    }

    /**
     * Hashwert aufgrund der ID.
     * @return Der Hashwert.
     */
    override fun hashCode() = id?.hashCode() ?: this::class.hashCode()

    /**
     * Konstante für Named Queries.
     */
    companion object {
        private const val PREFIX = "Login."

        /**
         * Name für die Named Query, mit der User anhand des Usernamens gesucht werden
         */
        const val BY_USERNAME = "${PREFIX}byUsername"

        /**
         * Name für die Named Query, mit der ermittelt wird, ob es bereits einen User mit gegebenem Usernamen gibt
         */
        const val USERNAME_EXISTS = "${PREFIX}usernameExists"

        /**
         * Parametername für Named Queries mit dem Usernamen
         */
        const val PARAM_USERNAME = "username"
    }
}
