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

import com.acme.fahrzeug.rest.CustomUserDTO
import com.acme.fahrzeug.rest.FahrzeugGetController.Companion.API_PATH
import com.acme.fahrzeug.security.AuthController.Companion.AUTH_PATH
import io.smallrye.mutiny.converters.uni.UniReactorConverters
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

/**
 * REST-Controller für die Abfrage von Werten (für "Software Engineering").
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Suppress("DuplicatedCode")
@RestController
@RequestMapping("$API_PATH$AUTH_PATH")
@Tag(name = "Authentifizierung API")
class AuthController(private val factory: SessionFactory, private val passwordEncoder: PasswordEncoder) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * "Einloggen" bei _Basic Authentication_.
     * @param customUserDTO Benutzerkennung und Passwort.
     * @return Response mit der Collection der Rollen oder Statuscode 401.
     */
    @PostMapping(path = ["/login"], produces = [TEXT_PLAIN_VALUE])
    @Operation(summary = "Einloggen bei Basic Authentifizierung", tags = ["Auth"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Eingeloggt"),
        ApiResponse(responseCode = "401", description = "Fehler bei Username oder Passwort"),
    )
    suspend fun login(customUserDTO: CustomUserDTO): ResponseEntity<Flow<String>> {
        logger.debug("login: customUserDTO={}", customUserDTO)
        val (username, password) = customUserDTO

        val rollen = findByUsername(username)
            .filter { user -> passwordEncoder.matches(password, user.password) }
            .map { user ->
                user.rollen?.map { rolle -> "\"$rolle\"" }
            }
            .map { rollen ->
                logger.debug("login: rollen={}", rollen)
                // [...] durch List.toString()
                flowOf(rollen.toString())
            }
            .firstOrNull() ?: return status(UNAUTHORIZED).build()

        return ok(rollen)
    }

    private fun findByUsername(username: String) = factory.withSession { session ->
        session.createNamedQuery<Login>(Login.BY_USERNAME)
            .setParameter(Login.PARAM_USERNAME, username)
            .singleResultOrNull
    }
        .convert()
        .with(UniReactorConverters.toFlux())
        .asFlow()

    /**
     * Die Rollen zur eigenen Benutzerkennung ermitteln.
     * @param principal Benutzerkennung als Objekt zum Interface Principal.
     * @return Response mit den eigenen Rollen oder Statuscode 401, falls man nicht eingeloggt ist.
     */
    @GetMapping(path = ["/rollen"], produces = [TEXT_PLAIN_VALUE])
    @Operation(summary = "Abfrage der eigenen Rollen", tags = ["Auth"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Rollen ermittelt"),
        ApiResponse(responseCode = "401", description = "Fehler bei Authentifizierung"),
    )
    suspend fun findEigeneRollen(principal: Principal?): ResponseEntity<Flow<String>> {
        if (principal == null) {
            return status(UNAUTHORIZED).build()
        }

        val username = principal.name
        logger.debug("findEigeneRollen: username={}", username)

        val rollen = findByUsername(username)
            .map { user ->
                user.rollen?.map { rolle -> "\"$rolle\"" }
            }
            .map { rollen ->
                logger.debug("findEigeneRollen: rollen={}", rollen)
                // [...] durch List.toString()
                flowOf(rollen.toString())
            }
            .first()

        return ok(rollen)
    }

    /**
     * Konstante für [AuthController].
     */
    companion object {
        /**
         * Pfad für Authentifizierung und Autorisierung
         */
        const val AUTH_PATH = "/auth"
    }
}
