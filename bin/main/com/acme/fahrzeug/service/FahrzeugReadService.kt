package com.acme.fahrzeug.service

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugId
import com.acme.fahrzeug.security.CustomUserDetailsService
import com.acme.fahrzeug.security.Rolle
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withTimeout
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.hibernate.reactive.mutiny.find
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.bind.Bindable.listOf
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import javax.persistence.NoResultException

// https://thorben-janssen.com/hibernate-reactive-getting-started-guide
// https://smallrye.io/smallrye-mutiny/guides/kotlin

/**
 * Anwendungslogik für das Lesen von Kunden.
 *
 * ![Klassendiagramm](../../../images/KundeReadService.svg)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class FahrzeugReadService(
    private val factory: SessionFactory,
    @Lazy private val queryBuilder: QueryBuilder,
    @Lazy private val userService: CustomUserDetailsService,
) {
    private val logger = LoggerFactory.getLogger(FahrzeugReadService::class.java)

    /**
     * Ein Fahrzeug anhand seiner ID suchen.
     * @param id Die Id des gesuchten Fahrzeugs
     * @param username Der username beim Login
     * @return [FindByIdResult.Success], im Fehlerfall [FindByIdResult.NotFound] oder [FindByIdResult.AccessForbidden]
     */
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KUNDE')")
    suspend fun findById(id: FahrzeugId, username: String): FindByIdResult {
        logger.debug("findById: id={}, username={}", id, username)

        val fahrzeug = findById(id)

        if (fahrzeug != null && (fahrzeug.username == username || username == "gast")) {
            return FindByIdResult.Success(fahrzeug)
        }

        // es muss ein Objekt der Klasse UserDetails geben, weil der Benutzername beim Einloggen verwendet wurde
        val userDetails =
            userService.findByUsername(username).awaitSingleOrNull() ?: return FindByIdResult.AccessForbidden()
        val rollen = userDetails
            .authorities
            .map { grantedAuthority -> grantedAuthority.authority }

        return if (!rollen.contains(Rolle.adminStr)) {
            FindByIdResult.AccessForbidden(rollen)
        } else if (fahrzeug == null) {
            FindByIdResult.NotFound(id)
        } else {
            FindByIdResult.Success(fahrzeug)
        }
    }

    /**
     * Ein Fahrzeug anhand seiner ID suchen. public für FahrzeugWriteService
     * @param id Die Id des gesuchten Fahrzeugs
     * @return Das gefundene Fahrzeug oder null.
     */
    suspend fun findById(id: FahrzeugId): Fahrzeug? {
        // https://hibernate.org/reactive/documentation/1.1/reference/html_single
        // https://thorben-janssen.com/hibernate-reactive-getting-started-guide
        // https://smallrye.io/smallrye-mutiny/guides/kotlin
        val fahrzeug = withTimeout(timeoutShort) {
            factory.withSession { session ->
                // session.find(Fahrzeug::class.java, id)
                session.find<Fahrzeug>(id) // Lesen: keine Transaktion erforderlich
            }.awaitSuspending()
        }
        logger.debug("findById: {}", fahrzeug)
        return fahrzeug
    }

    /**
     * Fahrzeuge anhand von Suchkriterien ermitteln.
     * Z.B. mit GET https://localhost:8080/api?nachname=A&plz=7
     * @param suchkriterien Suchkriterien.
     * @return Gefundene Fahrzeuge
     */
    @Suppress("ReturnCount")
    suspend fun find(suchkriterien: MultiValueMap<String, String>): Collection<Fahrzeug> {
        logger.debug("find: suchkriterien={}", suchkriterien)

        if (suchkriterien.isEmpty()) {
            return findAll()
        }

        if (suchkriterien.size == 1) {
            val beschreibung = suchkriterien["beschreibung"]
            if (beschreibung?.size == 1) {
                return findByBeschreibung(beschreibung[0])
            }

            val kennzeichen = suchkriterien["kennzeichen"]
            if (kennzeichen?.size == 1) {
                val fahrzeug = findByKennzeichen(kennzeichen[0]) ?: return emptyList()
                return listOf(fahrzeug)
            }
        }

        val criteriaQuery = when (val builderResult = queryBuilder.build(suchkriterien)) {
            is QueryBuilderResult.Success -> builderResult.criteriaQuery
            is QueryBuilderResult.Failure -> return emptyList()
        }

        val fahrzeuge = withTimeout(timeoutLong) {
            factory.withSession { session ->
                session.createQuery(criteriaQuery).resultList // Lesen: keine Transaktion erforderlich
            }.awaitSuspending()
        }
        logger.debug("find: {}", fahrzeuge)

        return fahrzeuge
    }

    private suspend fun findAll(): Collection<Fahrzeug> = withTimeout(timeoutLong) {
        factory.withSession { session ->
            session.createNamedQuery<Fahrzeug>(Fahrzeug.ALL)
                .resultList
        }.awaitSuspending()
    }

    private suspend fun findByBeschreibung(beschreibung: String): Collection<Fahrzeug> = withTimeout(timeoutShort) {
        factory.withSession { session ->
            session.createNamedQuery<Fahrzeug>(Fahrzeug.BY_BESCHREIBUNG)
                .setParameter(Fahrzeug.PARAM_BESCHREIBUNG, "%$beschreibung%")
                .resultList
        }.awaitSuspending()
    }

    @Suppress("SwallowedException")
    private suspend fun findByKennzeichen(kennzeichen: String) = try {
        withTimeout(timeoutShort) {
            factory.withSession { session ->
                // session.createNamedQuery(Kunde.BY_EMAIL, Kunde::class.java)
                session.createNamedQuery<Fahrzeug>(Fahrzeug.BY_KENNZEICHEN)
                    .setParameter(Fahrzeug.PARAM_KENNZEICHEN, kennzeichen)
                    .singleResult
            }.awaitSuspending()
        }
    } catch (e: NoResultException) {
        logger.debug("Keine Fahrzeuge mit dem Kennzeichen'{}'", kennzeichen)
        null
    }

    /**
     * Abfrage, welche Beschreibungen es zu einem Präfix gibt.
     * @param prefix Beshcreibung-Präfix als Pfadvariable.
     * @return Die passenden Beshcreibung oder Statuscode 404, falls es keine gibt.
     */
    suspend fun findBeschreibungenByPrefix(prefix: String): Collection<String> = withTimeout(timeoutShort) {
        factory.withSession { session ->
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            session.createNamedQuery<java.lang.String>(Fahrzeug.BESCHREIBUNG_PREFIX)
                .setParameter(Fahrzeug.PARAM_BESCHREIBUNG, "$prefix%")
                .resultList
                .map { list ->
                    list.map { str -> "$str" }
                }
        }.awaitSuspending()
    }

    private companion object {
        const val timeoutShort = 500L
        const val timeoutLong = 2000L
    }
}
