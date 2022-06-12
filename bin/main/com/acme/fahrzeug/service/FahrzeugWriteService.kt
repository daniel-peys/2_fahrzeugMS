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
package com.acme.fahrzeug.service

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.Fahrzeug.Companion.KENNZEICHEN_EXISTS
import com.acme.fahrzeug.entity.Fahrzeug.Companion.PARAM_KENNZEICHEN
import com.acme.fahrzeug.entity.FahrzeugId
import com.acme.fahrzeug.mail.Mailer
import com.acme.fahrzeug.mail.SendResult
import com.acme.fahrzeug.security.ConvertLoginResult
import com.acme.fahrzeug.security.CustomUser
import com.acme.fahrzeug.security.CustomUserDetailsService
import com.acme.fahrzeug.security.Login
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.withTimeout
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.hibernate.reactive.mutiny.find
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Suppress("TooManyFunctions")
/**
 * Anwendungslogik für das Schreiben von Fahrzeugen.
 *
 * ![Klassendiagramm](../../../images/KundeWriteService.svg)
 */
@Service
class FahrzeugWriteService(
    private val factory: SessionFactory,
    @Lazy private val validator: FahrzeugValidator,
    @Lazy private val userService: CustomUserDetailsService,
    @Lazy private val readService: FahrzeugReadService,
    @Lazy private val mailer: Mailer,
) {
    private val logger = LoggerFactory.getLogger(FahrzeugWriteService::class.java)

    /**
     * Einen neues Fahrzeug anlegen.
     * @param fahrzeug Das Objekt des neu anzulegenden Fahrzeugen
     * @return Der neu angelegte Fahrzeug mit generierter ID.
     */
    @Suppress("ReturnCount")
    suspend fun create(fahrzeug: Fahrzeug): CreateResult {  // , user: CustomUser
        logger.debug("create: {}", fahrzeug)
        //logger.debug("create: {}", user)
        val violations = validator.validate(fahrzeug)
        if (violations.isNotEmpty()) {
            return CreateResult.ConstraintViolations(violations)
        }
        logger.trace("create: Keine \"Constraint Violations\"")

        val kennzeichen = fahrzeug.kennzeichen
        if (kennzeichenExists(kennzeichen)) {
            return CreateResult.KennzeichenExists(kennzeichen)
        }
        logger.trace("create: Kennzeichen noch nicht vorhanden")

        val login: Login
        when (val result = userService.convertLogin(user)) {
            is ConvertLoginResult.Success -> {
                login = result.login
                logger.trace("create: login={}", login)
            }
            is ConvertLoginResult.Invalid -> return CreateResult.InvalidUser(result.invalidUser)
            is ConvertLoginResult.UsernameExists -> return CreateResult.UsernameExists(result.username)
        }

        val neuesFahrzeug = fahrzeug.copy(username = user.username)
        withTimeout(timeoutLong) {
            // https://hibernate.org/reactive/documentation/1.1/reference/html_single/#_transactions
            // kein Transaktionsobjekt erforderlich, da es keine Bedingung gibt, die zu Rollback fuehren kann
            // zuerst den Fahrzeuge abspeichern, dann die Benutzerdaten
            factory.withTransaction { session, _ ->
                session.persist(login)
                    .chain { _ -> session.persist(neuesFahrzeug) }
            }.awaitSuspending()
        }

        when (val sendResult = mailer.send(neuesFahrzeug)) {
            is SendResult.Success -> logger.debug("create: Email gesendet")
            else -> {
                // TODO Erneuter Versuch, die Email zu senden
                logger.warn("create: Email nicht gesendet: Ist der Mailserver erreichbar?")
                return CreateResult.SuccessWithoutEmail(neuesFahrzeug, sendResult)
            }
        }
        return CreateResult.Success(neuesFahrzeug)
    }

    private suspend fun kennzeichenExists(kennzeichen: String) = withTimeout(timeoutShort) {
        factory.withSession { session ->
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            session.createNamedQuery<java.lang.Long>(KENNZEICHEN_EXISTS)
                .setParameter(PARAM_KENNZEICHEN, kennzeichen)
                .singleResult
                .map { it > 0 }
        }
            .awaitSuspending()
    }

    /**
     * Ein vorhandenes Fahrzeug aktualisieren.
     * @param fahrzeug Das Objekt mit den neuen Daten.
     * @param id ID des Fahrzeugs
     * @param versionStr Versionsnummer.
     * @return Der aktualisierte Fahrzeug oder null, falls es kein Fahrzeug mit der angegebenen ID gibt.
     */
    @Suppress("KDocUnresolvedReference", "ReturnCount")
    suspend fun update(fahrzeug: Fahrzeug, id: FahrzeugId, versionStr: String): UpdateResult {
        logger.debug("update: {}", fahrzeug)
        logger.debug("update: id={}, versionStr={}", id, versionStr)
        val violations = validator.validate(fahrzeug)
        if (violations.isNotEmpty()) {
            return UpdateResult.ConstraintViolations(violations)
        }

        val fahrzeugDb = readService.findById(id) ?: return UpdateResult.NotFound
        logger.trace("update: version={}, fahrzeugDb={}", versionStr, fahrzeugDb)

        val version = versionStr.toIntOrNull() ?: return UpdateResult.VersionInvalid(versionStr)
        // BEACHTE: Hibernate wirft keine OptimisticEntityLockException bei veralteter Version
        if (version != fahrzeugDb.version) {
            return UpdateResult.VersionOutdated(version)
        }

        val kennzeichen = fahrzeug.kennzeichen
        if (kennzeichenExists(fahrzeugDb, kennzeichen)) {
            return UpdateResult.KennzeichenExists(kennzeichen)
        }

        return update(fahrzeug, fahrzeugDb)
    }

    private suspend fun kennzeichenExists(fahrzeugDb: Fahrzeug, neuesKennzeichen: String): Boolean {
        // Hat sich das Kennzeichen ueberhaupt geaendert?
        if (fahrzeugDb.kennzeichen == neuesKennzeichen) {
            logger.trace("kennzeichenExists: Kennzeichen nicht geaendert: {}", neuesKennzeichen)
            return false
        }

        logger.trace("kennzeichenExists: Kennzeichen geaendert: {} -> {}", fahrzeugDb.kennzeichen, neuesKennzeichen)
        // Gibt es die neue Emailadresse bei einem existierenden Kunden?
        return kennzeichenExists(neuesKennzeichen)
    }

    private suspend fun update(fahrzeug: Fahrzeug, fahrzeugDb: Fahrzeug): UpdateResult {
        fahrzeugDb.set(fahrzeug)

        logger.trace("update: vor session.merge() = {}", fahrzeugDb)
        val result = withTimeout(timeoutLong) {
            factory.withTransaction { session, _ ->
                session.detach(fahrzeugDb)
                session.merge(fahrzeugDb)
            }.awaitSuspending()
        }
        logger.trace("update: nach session.merge(): {}", result)

        return UpdateResult.Success(result)
    }

    /**
     * Einen vorhandenes Fahrzeug in der DB löschen.
     * @param id Die ID des zu löschenden Fahrzeugs
     */
    // @PreAuthorize("hasRole('ADMIN')")
    @Suppress("HasPlatformType")
    suspend fun deleteById(id: FahrzeugId) {
        withTimeout(timeoutShort) {
            logger.debug("deleteById: id={}", id)
            factory.withTransaction { session, _ ->
                session.find<Fahrzeug>(id)
                    .chain { fahrzeug ->
                        when (fahrzeug) {
                            null -> {
                                logger.trace("deleteById: kein Fahrzeug gefunden")
                                Uni.createFrom().nullItem()
                            }
                            else -> {
                                logger.trace("deleteById: {}", fahrzeug)
                                session.remove(fahrzeug)
                            }
                        }
                    }
            }.awaitSuspending()
        }
    }

    private companion object {
        const val timeoutShort = 500L
        const val timeoutLong = 1000L
    }
}
