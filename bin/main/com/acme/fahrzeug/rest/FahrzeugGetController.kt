package com.acme.fahrzeug.rest

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugId
import com.acme.fahrzeug.rest.FahrzeugGetController.Companion.API_PATH
import com.acme.fahrzeug.service.FahrzeugReadService
import com.acme.fahrzeug.service.FindByIdResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Funktionen der Klasse abgebildet werden
 * @constructor Einen FahrzeugGetController mit einem injizierten [FahrzeugReadService] erzeugen.
 */
@RestController
@RequestMapping(API_PATH)
@Tag(name = "Fahrzeug API")
@Suppress("RegExpUnexpectedAnchor")
class FahrzeugGetController(private val service: FahrzeugReadService) {
    private val logger = LoggerFactory.getLogger(FahrzeugGetController::class.java)

    /**
     * Suche anhand der Fahrzeug-ID
     * @param id ID des gesuchten Fahzeugs.
     * @param version Versionsnummer aus dem Header `If-None-Match`
     * @param request Request-Objekt für die HATEOAS-Links
     * @param principal Principal-Objekt für Security
     * @return Ein ServerResponse mit dem Statuscode 200 und dem gefundenenFahrzeuges einschließlich HATEOAS-Links, oder
     *      aber Statuscode 404.
     */
    @GetMapping(path = ["/{id:$ID_PATTERN}"], produces = [HAL_JSON_VALUE])
    @Operation(summary = "Suche mit der Fahrzeug-ID", tags = ["Suchen"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fahrzeug gefunden"),
        ApiResponse(responseCode = "304", description = "Fahrzeug unverändert"),
        ApiResponse(responseCode = "404", description = "Fahrzeug nicht gefunden"),
        ApiResponse(responseCode = "403", description = "Fehlende Zugriffsrechte"),
        ApiResponse(responseCode = "406", description = "Falsche Versionsnummer im ETag"),
    )
    // https://localhost:8080/swagger-ui.html
    suspend fun findById(
        @PathVariable id: FahrzeugId,
        @RequestHeader("If-None-Match") version: String?,
        request: ServerHttpRequest,
        principal: Principal?,
    ): ResponseEntity<Any> {
        logger.trace("findById: id={}, version={}", id, version)
        val username = principal?.name ?: "gast" // return status(FORBIDDEN).build()

        return when (val result = service.findById(id, username)) {
            is FindByIdResult.Success -> handleFound(result.fahrzeug, version, request)
            is FindByIdResult.NotFound -> notFound().build()
            is FindByIdResult.AccessForbidden -> status(FORBIDDEN).build()
        }
    }

    private fun handleFound(fahrzeug: Fahrzeug, version: String?, request: ServerHttpRequest): ResponseEntity<Any> {
        logger.trace("handleFound: {}", fahrzeug)
        if (version != null && version.first() != '"' && version.last() != '"') {
            return status(NOT_ACCEPTABLE).body(GenericBody.Text("Ungueltige Versionsnummer $version"))
        }
        val currentVersion = "\"${fahrzeug.version}\""
        if (version == currentVersion) {
            return status(NOT_MODIFIED).build()
        }

        val model = FahrzeugModel(fahrzeug)
        addLinks(model, fahrzeug.id, request)
        // Entity Tag, um Aenderungen an der angeforderten
        // Ressource erkennen zu koennen.
        // Client: GET-Requests mit Header "If-None-Match"
        //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
        return ok().eTag(currentVersion).body(model)
    }

    private fun addLinks(model: FahrzeugModel, id: FahrzeugId?, request: ServerHttpRequest) = with(model) {
        val baseUri = getBaseUri(request.headers, request.uri, id)
        val idUri = "$baseUri/$id"
        val selfLink = Link.of(idUri)
        val listLink = Link.of(baseUri, LinkRelation.of("list"))
        val addLink = Link.of(baseUri, LinkRelation.of("add"))
        val updateLink = Link.of(idUri, LinkRelation.of("update"))
        val removeLink = Link.of(idUri, LinkRelation.of("remove"))
        add(selfLink, listLink, addLink, updateLink, removeLink)
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter.
     * @param queryParams Query-Parameter als MultiValueMap für z.B. /api?beschreibung=L.
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und den gefundenen Fahrzeugen als CollectionModel
     * oder Statuscode 404.
     */
    @GetMapping(produces = [HAL_JSON_VALUE])
    @Operation(summary = "Suche mit Suchkriterien", tags = ["Suchen"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "CollectionModel mid den Fahrzeugen"),
        ApiResponse(responseCode = "404", description = "Keine Fahrzeuge gefunden"),
    )
    suspend fun find(
        @RequestParam queryParams: MultiValueMap<String, String>,
        request: ServerHttpRequest,
    ): ResponseEntity<CollectionModel<FahrzeugModel>> {
        logger.debug("find: queryParams={}", queryParams)
        val baseUri = getBaseUri(request.headers, request.uri)
        val models = service.find(queryParams)
            .map { fahrzeug ->
                val model = FahrzeugModel(fahrzeug)
                val selfLink = Link.of("$baseUri/${fahrzeug.id}")
                model.add(selfLink)
            }
        logger.debug("find: {}", models)

        if (models.isEmpty()) {
            return notFound().build()
        }
        return ok(CollectionModel.of(models))
    }

    /**
     * Abfrage, welche Beschreibung es zu einem Präfix gibt.
     * @param prefix Beschreibung-Präfix als Pfadvariable.
     * @return Die passenden Beschreibungen oder Statuscode 404, falls es keine gibt.
     */
    @GetMapping(path = ["$BESCHREIBUNGEN_PATH/{prefix}"])
    suspend fun findBeschreibungenByPrefix(@PathVariable prefix: String): ResponseEntity<String> {
        logger.debug("findBeschreibungenByPrefix: prefix={}", prefix)
        val beschreibungen = service.findBeschreibungenByPrefix(prefix)
        logger.debug("findBeschreibungenByPrefix: beschreibungen={}", beschreibungen)

        if (beschreibungen.isEmpty()) {
            return notFound().build()
        }
        return ok(beschreibungen.toString())
    }

    /**
     * Konstante für [FahrzeugGetController].
     */
    companion object {
        /**
         * Basispfad der REST-Schnittstelle.
         */
        const val API_PATH = "/api"

        private const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster für eine UUID. `$HEX_PATTERN{8}-($HEX_PATTERN{4}-){3}$HEX_PATTERN{12}` enthält eine _capturing group_
         * und ist nicht zulässig.
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"

        /**
         * Pfad, um Nachnamen abzufragen
         */
        const val BESCHREIBUNGEN_PATH = "/beschreibung"
    }
}
