package com.acme.fahrzeug.service

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugType
import com.acme.fahrzeug.entity.Fahrzeughalter
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.criteria.createQuery
import javax.persistence.criteria.from
import javax.persistence.criteria.get

// https://howtodoinjava.com/hibernate/hibernate-criteria-queries-tutorial
// https://www.baeldung.com/hibernate-criteria-queries
// https://lifeinide.com/post/2021-04-29-making-jpa-criteria-api-less-awkward-with-kotlin/#typesafe-entity-fields-access

// BEACHTE: Metamodel-Klassen von Hibernate benötigen einen Annotation-Processor, wie z.B. apt (Java) oder kapt (Kotlin)
// ABER: kapt für Kotlin ist deprecated und unterstuetzt *nicht* das neue Compiler-IR-Backend von Kotlin

/**
 * Singleton-Klasse, um _Criteria Queries_ für _Hibernate_ zu bauen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Suppress("TooManyFunctions")
@Service
class QueryBuilder(private val factory: SessionFactory) {
    private val logger: Logger = LoggerFactory.getLogger(QueryBuilder::class.java)

    /**
     * Aus einer `MultiValueMap` von _Spring_ wird eine Criteria Query für Hibernate gebaut, um flexibel nach fahrzeugen
     * suchen zu können.
     * @param queryParams Die Query-Parameter in einer `MultiValueMap`.
     * @return [QueryBuilderResult] abhängig von den Query-Parametern.
     */
    fun build(queryParams: MultiValueMap<String, String>): QueryBuilderResult {
        logger.debug("build: queryParams={}", queryParams)

        val criteriaBuilder = factory.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery<Fahrzeug>() // session.createQuery(Fahrzeug::class.java)
        val fahrzeugRoot = criteriaQuery.from(Fahrzeug::class) // criteriaQuery.from(Fahrzeug::class.java)

        if (queryParams.isEmpty()) {
            // keine Suchkriterien
            return QueryBuilderResult.Success(criteriaQuery)
        }

        val predicates = queryParams.map { (paramName, paramValues) ->
            getPredicate(paramName, paramValues, criteriaBuilder, fahrzeugRoot)
        }.filterNotNull()

        if (predicates.isEmpty()) {
            return QueryBuilderResult.Failure
        }
        logger.debug("build: #predicates={}", predicates.size)

        @Suppress("SpreadOperator")
        val predicate = criteriaBuilder.and(*predicates.toTypedArray()) // variable Argumentleiste
        criteriaQuery.where(predicate)

        return QueryBuilderResult.Success(criteriaQuery)
    }

    private fun getPredicate(
        paramName: String,
        paramValues: Collection<String>?,
        criteriaBuilder: CriteriaBuilder,
        fahrzeugRoot: Root<Fahrzeug>,
    ): Predicate? {
        if (paramValues?.size != 1) {
            return null
        }

        logger.debug("getPredicate: propertyValues={}", paramValues)

        val value = paramValues.first()
        return when (paramName) {
            beschreibung -> getPredicateBeschreibung(value, criteriaBuilder, fahrzeugRoot)
            kennzeichen -> getPredicateKennzeichen(value, criteriaBuilder, fahrzeugRoot)
            vorname -> getPredicateFahrzeughalterVorname(value, criteriaBuilder, fahrzeugRoot)
            nachname -> getPredicateFahrzeughalterNachname(value, criteriaBuilder, fahrzeugRoot)
            fahrzeugtyp -> getPredicateFahrzeugtype(value, criteriaBuilder, fahrzeugRoot)
            else -> null
        }
    }

    // Nachname: Suche nach Teilstrings
    private fun getPredicateBeschreibung(
        neschreibung: String,
        criteriaBuilder: CriteriaBuilder,
        fahrzeugRoot: Root<Fahrzeug>,
    ) =
        criteriaBuilder.like(fahrzeugRoot.get(Fahrzeug::beschreibung), "%$neschreibung%")

    // Email: Suche mit Teilstring ohne Gross-/Kleinschreibung
    private fun getPredicateKennzeichen(
        kennzeichen: String,
        criteriaBuilder: CriteriaBuilder,
        fahrzeugRoot: Root<Fahrzeug>,
    ) =
        criteriaBuilder.like(fahrzeugRoot.get(Fahrzeug::kennzeichen), "%$kennzeichen%")

    // PLZ: Suche mit Praefix
    private fun getPredicateFahrzeughalterVorname(
        vorname: String,
        criteriaBuilder: CriteriaBuilder,
        fahrzeugRoot: Root<Fahrzeug>,
    ) =
        criteriaBuilder.like(fahrzeugRoot.get(Fahrzeug::fahrzeughalter).get(Fahrzeughalter::vorname), "$vorname%")

    // Ort: Suche mit Praefix
    private fun getPredicateFahrzeughalterNachname(
        nachname: String,
        criteriaBuilder: CriteriaBuilder,
        fahrzeugRoot: Root<Fahrzeug>,
    ) =
        criteriaBuilder.like(fahrzeugRoot.get(Fahrzeug::fahrzeughalter).get(Fahrzeughalter::nachname), "$nachname%")

    private fun getPredicateFahrzeugtype(
        fahrzeugtypeValue: String,
        criteriaBuilder: CriteriaBuilder,
        fahrzeugRoot: Root<Fahrzeug>,
    ) =
        criteriaBuilder.equal(
            fahrzeugRoot.get(Fahrzeug::fahrzeugtyp),
            FahrzeugType.fromValue(fahrzeugtypeValue),
        )

    private companion object {
        private const val beschreibung = "beschreibung"
        private const val kennzeichen = "kennzeichen"
        private const val vorname = "vorname"
        private const val nachname = "nachname"
        private const val fahrzeugtyp = "fahrzeugtyp"
    }
}

/**
 * Resultat-Typ für [QueryBuilder.build]
 */
sealed interface QueryBuilderResult {
    /**
     * Resultat-Typ, wenn die Query-Parameter korrekt sind.
     * @property criteriaQuery Die CriteriaQuery
     */
    data class Success(val criteriaQuery: CriteriaQuery<Fahrzeug>) : QueryBuilderResult

    /**
     * Resultat-Typ, wenn mindestens 1 Query-Parameter falsch ist.
     */
    object Failure : QueryBuilderResult
}
