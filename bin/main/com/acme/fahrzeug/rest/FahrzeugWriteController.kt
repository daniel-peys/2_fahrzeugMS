/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import am.ik.yavi.core.ConstraintViolation
import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugId
import com.acme.fahrzeug.rest.FahrzeugGetController.Companion.API_PATH
import com.acme.fahrzeug.rest.FahrzeugGetController.Companion.ID_PATTERN
import com.acme.fahrzeug.rest.patch.FahrzeugPatcher
import com.acme.fahrzeug.rest.patch.PatchOperation
import com.acme.fahrzeug.service.CreateResult
import com.acme.fahrzeug.service.FahrzeugReadService
import com.acme.fahrzeug.service.FahrzeugWriteService
import com.acme.fahrzeug.service.FindByIdResult
import com.acme.fahrzeug.service.UpdateResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.PRECONDITION_FAILED
import org.springframework.http.HttpStatus.PRECONDITION_REQUIRED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.status
import org.springframework.http.ResponseEntity.unprocessableEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.security.Principal

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Funktionen der Klasse abgebildet werden
 * @constructor Einen FahrzeugWriteController mit einem injizierten [FahrzeugWriteService] erzeugen.
 */
@RestController
@RequestMapping(API_PATH)
@Tag(name = "Fahrzeug API")
@Suppress("TooManyFunctions", "LargeClass", "RegExpUnexpectedAnchor")
class FahrzeugWriteController(
    private val service: FahrzeugWriteService,
    @Lazy private val readService: FahrzeugReadService,
) {
    private val logger = LoggerFactory.getLogger(FahrzeugWriteController::class.java)

    // @param fahrzeugUserDTO Das Fahrzeugnobjekt mit den Benutzerdaten aus dem eingegangenen Request-Body.
    /**
     * Einen neuen Fahrzeug-Datensatz anlegen.
     * @param request Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 422 falls Constraints verletzt
     *      sind oder der JSON-Datensatz syntaktisch nicht korrekt ist oder falls die Emailadresse bereits existiert.
     */
    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    @Operation(summary = "Ein neues Fahrzeug anlegen", tags = ["Neu anlegen"])
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Fahrzeug neu angelegt"),
        ApiResponse(responseCode = "422", description = "Ungültige Werte oder Kennzeichen/Username vorhanden"),
    )
    suspend fun create(
        // @RequestBody fahrzeugUserDTO: FahrzeugUserDTO,
        @RequestBody fahrzeugDTO: FahrzeugDTO,
        request: ServerHttpRequest,
    ): ResponseEntity<GenericBody> {
        // logger.debug("create: {}", fahrzeugUserDTO)
        logger.debug("create: {}", fahrzeugDTO)

        return when (
            val result =
                // service.create(fahrzeugUserDTO.fahrzeugDTO.toFahrzeug(), fahrzeugUserDTO.userDTO.toCustomUser())
                service.create(fahrzeugDTO.toFahrzeug())
        ) {
            is CreateResult.Success -> handleCreateSuccess(result.fahrzeug, request)

            is CreateResult.SuccessWithoutEmail -> handleCreateSuccess(result.fahrzeug, request)

            is CreateResult.ConstraintViolations -> handleConstraintViolations(result.violations)

            is CreateResult.InvalidUser -> unprocessableEntity().body(GenericBody.Text("Ungueltige Benutzerdaten"))

            is CreateResult.UsernameExists ->
                unprocessableEntity().body(GenericBody.Text("Der Username ${result.username} existiert bereits"))

            is CreateResult.KennzeichenExists ->
                unprocessableEntity().body(GenericBody.Text("Das Kennzeichen ${result.kennzeichen} existiert bereits"))
        }
    }

    private fun handleCreateSuccess(fahrzeug: Fahrzeug, request: ServerHttpRequest): ResponseEntity<GenericBody> {
        logger.trace("handleCreateSuccess: {}", fahrzeug)
        val baseUri = getBaseUri(request.headers, request.uri)
        val location = URI("$baseUri/${fahrzeug.id}")
        logger.trace("handleCreateSuccess: location={}", location)
        return created(location).build()
    }

    // z.B. Service-Funktion "create|update" mit Param "fahrzeug" hat dann Meldungen mit "create.fahrzeug.kennzeichen:"
    private fun handleConstraintViolations(violations: Collection<ConstraintViolation>): ResponseEntity<GenericBody> {
        if (violations.isEmpty()) {
            return unprocessableEntity().build()
        }

        val fahrzeugViolations = violations.associate { violation ->
            violation.messageKey() to violation.message()
        }
        logger.trace("mapConstraintViolations(): {}", fahrzeugViolations)

        return unprocessableEntity().body(GenericBody.Values(fahrzeugViolations))
    }

    /**
     * Einen vorhandenen Fahrzeug-Datensatz überschreiben.
     * @param id ID des zu aktualisierenden Fahrzeuges
     * @param fahrzeugDTO Das Fahrzeugnobjekt aus dem eingegangenen Request-Body.
     * @param version Versionsnummer aus dem Header `If-Match`
     * @return Response mit Statuscode 204 oder Statuscode 422 (falls Constraints verletzt sind oder
     *      der JSON-Datensatz syntaktisch nicht korrekt ist oder falls das Kennzeichen bereits existiert)
     *      oder 412 falls die Versionsnummer nicht ok ist oder 428 falls die Versionsnummer fehlt.
     */
    @PutMapping(path = ["/{id:$ID_PATTERN}"], consumes = [APPLICATION_JSON_VALUE])
    @Operation(summary = "Ein Fahrzeug mit neuen Werten aktualisieren", tags = ["Aktualisieren"])
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Aktualisiert"),
        ApiResponse(responseCode = "404", description = "Fahrzeug nicht vorhanden"),
        ApiResponse(responseCode = "412", description = "Versionsnummer falsch"),
        ApiResponse(responseCode = "422", description = "Ungültige Werte oder Kennzeichen vorhanden"),
        ApiResponse(responseCode = "428", description = VERSIONSNUMMER_FEHLT),
    )
    suspend fun update(
        @PathVariable id: FahrzeugId,
        @RequestBody fahrzeugDTO: FahrzeugUpdateDTO,
        @RequestHeader("If-Match") version: String?,
    ): ResponseEntity<out GenericBody> {
        logger.debug("update: id={}", id)
        logger.debug("update: {}", fahrzeugDTO)
        logger.debug("update: version={}", version)

        @Suppress("DuplicatedCode")
        if (version == null) {
            return status(PRECONDITION_REQUIRED).body(GenericBody.Text(VERSIONSNUMMER_FEHLT))
        }
        // Im Header:    If-Match: "1234"
        @Suppress("MagicNumber")
        if (version.length < 3) {
            return status(PRECONDITION_FAILED).body(GenericBody.Text("Falsche Versionsnummer $version"))
        }
        val versionStr = version.substring(1, version.length - 1)

        val result = service.update(fahrzeugDTO.toFahrzeug(), id, versionStr)
        return handleUpdateResult(result)
    }

    private fun handleUpdateResult(result: UpdateResult) =
        when (result) {
            is UpdateResult.Success -> noContent().eTag("\"${result.fahrzeug.version}\"").build()

            is UpdateResult.NotFound -> notFound().build()

            is UpdateResult.ConstraintViolations -> handleConstraintViolations(result.violations)

            is UpdateResult.VersionInvalid ->
                status(PRECONDITION_FAILED).body(GenericBody.Text("Falsche Versionsnummer ${result.version}"))

            is UpdateResult.VersionOutdated ->
                status(PRECONDITION_FAILED).body(GenericBody.Text("Veraltete Versionsnummer ${result.version}"))

            is UpdateResult.KennzeichenExists ->
                unprocessableEntity().body(GenericBody.Text("Das Kennzeichen $${result.kennzeichen} existiert bereits"))
        }

    /**
     * Einen vorhandenen Fahrzeug-Datensatz durch PATCH aktualisieren.
     * @param id ID des zu aktualisierenden Fahrzeuges
     * @param patchOps Die Collection der Patch-Operationen
     * @param version Versionsnummer aus dem Header `If-Match`
     * @param principal Principal-Objekt für Security
     * @return Response mit Statuscode 204 oder Statuscode 422 (falls Constraints verletzt sind oder
     *      der JSON-Datensatz syntaktisch nicht korrekt ist oder falls das Kennzeichenbereits existiert)
     *      oder 412 falls die Versionsnummer nicht ok ist oder 428 falls die Versionsnummer fehlt.
     */
    @PatchMapping(path = ["/{id:$ID_PATTERN}"], consumes = [APPLICATION_JSON_VALUE])
    @Operation(summary = "Ein Fahrzeug mit einzelnen neuen Werten aktualisieren", tags = ["Aktualisieren"])
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Aktualisiert"),
        ApiResponse(responseCode = "404", description = "Fahrzeug nicht vorhanden"),
        ApiResponse(responseCode = "412", description = "Versionsnummer falsch"),
        ApiResponse(responseCode = "422", description = "Ungültige Werte oder Kennzeichen vorhanden"),
        ApiResponse(responseCode = "428", description = VERSIONSNUMMER_FEHLT),
    )
    @Suppress("ReturnCount")
    suspend fun patch(
        @PathVariable id: FahrzeugId,
        @RequestBody patchOps: Collection<PatchOperation>,
        @RequestHeader("If-Match") version: String?,
        principal: Principal?,
    ): ResponseEntity<out GenericBody> {
        logger.debug("patch: id={}, patchOps={}, version={}", id, patchOps, version)

        if (version == null) {
            return status(PRECONDITION_REQUIRED).body(GenericBody.Text(VERSIONSNUMMER_FEHLT))
        }
        // Im Header:    If-Match: "1234"
        @Suppress("MagicNumber")
        if (version.length < 3) {
            return status(PRECONDITION_FAILED).body(GenericBody.Text("Falsche Versionsnummer $version"))
        }
        val versionStr = version.substring(1, version.length - 1)

        val username = principal?.name ?: return status(FORBIDDEN).build()
        val fahrzeug = when (val findByIdResult = readService.findById(id, username)) {
            is FindByIdResult.Success -> findByIdResult.fahrzeug
            is FindByIdResult.NotFound -> return notFound().build()
            is FindByIdResult.AccessForbidden -> return status(FORBIDDEN).build()
        }

        val patchedFahrzeug = FahrzeugPatcher.patch(fahrzeug, patchOps) ?: return badRequest().build()
        logger.trace("patch: Fahrzeug mit Patch-Ops: {}", patchedFahrzeug)
        val result = service.update(patchedFahrzeug, id, versionStr)
        return handleUpdateResult(result)
    }

    /**
     * Einen vorhandenes Fahrzeug anhand seiner ID löschen.
     * @param id ID des zu löschenden Fahrzeuges
     * @return Response mit Statuscode 204.
     */
    @DeleteMapping(path = ["/{id:$ID_PATTERN}"])
    @Operation(summary = "Ein Fahrzeug anhand der ID loeschen", tags = ["Loeschen"])
    @ApiResponses(ApiResponse(responseCode = "204", description = "Gelöscht"))
    suspend fun deleteById(@PathVariable id: FahrzeugId): ResponseEntity<Unit> {
        logger.debug("deleteById: id={}", id)
        service.deleteById(id)
        return noContent().build()
    }

    /**
     * Konstante für [FahrzeugWriteController].
     */
    private companion object {
        const val VERSIONSNUMMER_FEHLT = "Versionsnummer fehlt"
    }
}
