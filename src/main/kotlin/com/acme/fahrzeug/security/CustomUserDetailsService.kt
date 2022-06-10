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

import com.acme.fahrzeug.security.Login.Companion.BY_USERNAME
import com.acme.fahrzeug.security.Login.Companion.PARAM_USERNAME
import io.smallrye.mutiny.converters.uni.UniReactorConverters
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service-Klasse, um Benutzerkennungen zu suchen und neu anzulegen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class CustomUserDetailsService(
    private val factory: SessionFactory,
    @Lazy private val customUserValidator: CustomUserValidator,
    @Lazy private val passwordEncoder: PasswordEncoder,
) : ReactiveUserDetailsService {
    private val logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java).apply {
        debug("CustomUserDetailsService wird erzeugt")
    }

    /**
     * Zu einem gegebenen Username wird der zugehörige User gesucht.
     * @param username Username des gesuchten Users
     * @return Der gesuchte User in einem Mono
     */
    override fun findByUsername(username: String?): Mono<UserDetails?> {
        logger.debug("findByUsername: {}", username)
        return factory.withSession { session ->
            session.createNamedQuery(BY_USERNAME, Login::class.java)
                .setParameter(PARAM_USERNAME, username)
                .singleResultOrNull
        }
            .convert()
            // https://smallrye.io/smallrye-mutiny/guides/converters
            .with(UniReactorConverters.toMono())
            .map { it.toCustomUser() }
            .cast(UserDetails::class.java)
            .doOnNext { logger.debug("findByUsername: {}", it) }
    }

    /**
     * Ein Login-Objekt bauen, um es dann in der DB zuspeichern
     * @param user Der neu anzulegende User
     * @return Ein Resultatobjekt mit entweder dem neu gebauten Login oder mit
     *      einem Fehlerobjekt vom Typ [ConvertLoginResult.Invalid] oder [ConvertLoginResult.UsernameExists].
     */
    @Suppress("LongMethod", "KotlinConstantConditions")
    suspend fun convertLogin(user: CustomUser): ConvertLoginResult {
        logger.debug("convertLogin: {}", user)

        val isPasswordValid = customUserValidator.validatePassword(user).isValid
        if (!isPasswordValid) {
            logger.debug("convertLogin: isPasswordValid = {}", isPasswordValid)
            return ConvertLoginResult.Invalid(user)
        }

        val username = user.username
        val usernameExists = factory.withSession { session ->
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            session.createNamedQuery<java.lang.Long>(Login.USERNAME_EXISTS)
                .setParameter(PARAM_USERNAME, username)
                .singleResult
                .map { it > 0 }
        }.awaitSuspending()
        if (usernameExists) {
            return ConvertLoginResult.UsernameExists(username)
        }

        // Die Account-Informationen des Kunden transformieren: in Account-Informationen fuer die Security-Komponente
        val encodedPassword = passwordEncoder.encode(user.password)
        val rollen = user.authorities
            .map { grantedAuthority -> grantedAuthority.authority.substringAfter(Rolle.rolePrefix) }
            .toSet()
        val login = Login(
            username = username.lowercase(),
            password = encodedPassword,
            rollen = rollen,
        )

        logger.trace("convertLogin: login = {}", login)
        return ConvertLoginResult.Success(login)
    }
}

/**
 * Resultat-Typ für [CustomUserDetailsService.convertLogin]
 */
sealed interface ConvertLoginResult {
    /**
     * Resultat-Typ, wenn ein Login erfolgreich gebaut wurde.
     * @property login Das neu gebaute Login
     */
    data class Success(val login: Login) : ConvertLoginResult

    /**
     * Resultat-Typ, wenn ein User wegen eines unzureichenden Passworts nicht angelegt werden darf.
     * @property invalidUser Ungültiger User vom Typ [CustomUser]]
     */
    data class Invalid(val invalidUser: CustomUser) : ConvertLoginResult

    /**
     * Resultat-Typ, wenn eine Benutzerkennung nicht angelegt werdem kann, weil der Benutzername bereits existiert.
     * @property username Der bereits existierende Benutzername
     */
    data class UsernameExists(val username: String) : ConvertLoginResult
}
